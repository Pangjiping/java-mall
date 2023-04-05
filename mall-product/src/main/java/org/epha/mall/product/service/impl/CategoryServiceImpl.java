package org.epha.mall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.product.dao.CategoryDao;
import org.epha.mall.product.entity.CategoryEntity;
import org.epha.mall.product.service.CategoryBrandRelationService;
import org.epha.mall.product.service.CategoryService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    RedissonClient redissonClient;

    @Resource
    CategoryBrandRelationService categoryBrandRelationService;

    @Cacheable({"category"})
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Cacheable(value = {"category"},key = "@root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys(){
        log.debug("查询数据库...");

        List<CategoryEntity> entities = getBaseMapper().selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return entities;
    }

    /**
     * 使用redis改造这个接口，增加缓存
     * <p>
     * 1. 空结果缓存，解决缓存穿透
     * 2. 设置过期时间（加随机值），解决缓存雪崩
     * 3. 加锁，解决缓存击穿
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        // 加入缓存逻辑
        String catalog = stringRedisTemplate.opsForValue().get("catalog");

        if (StringUtils.isEmpty(catalog)) {
            log.debug("缓存未命中，开始加载数据库");
            // 缓存未命中，查数据库，并更新缓存
            List<CategoryEntity> categoryEntities = listWithTreeFromDbWithRedisson();

            return categoryEntities;
        }

        log.debug("缓存命中，直接返回");
        // 从缓存中拿数据，json反序列化
        return JSON.parseObject(catalog, new TypeReference<List<CategoryEntity>>() {
        });
    }

    /**
     * 使用本地锁，防止缓存击穿，但是在分布式环境下会有点问题，仍然会多次访问数据库
     * 在查完数据库之后，在加锁期间更新缓存
     *
     * @return
     */
    @Deprecated
    private List<CategoryEntity> listWithTreeFromDbWithLocalLocker() {

        synchronized (this) {
            return getDataFromDbAndSetIntoRedis();
        }
    }

    /**
     * 使用自己实现的redis分布式锁，防止缓存击穿
     *
     * @return
     */
    @Deprecated
    private List<CategoryEntity> listWithTreeFromDbWithRedisLocker() {

        // 抢占分布式锁
        String ownerToken = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent("listWithTreeFromDbWithRedisLocker-lock", ownerToken, 30, TimeUnit.SECONDS);
        if (locked == null || locked) {
            // 加锁成功，执行业务
            List<CategoryEntity> categoryEntities;
            // 防止出现异常中断解锁操作，使用try-finally结构
            try {
                categoryEntities = getDataFromDbAndSetIntoRedis();
            } finally {
                // 删除锁
                // 这种方式不能保证原子性
//            String redisToken = stringRedisTemplate.opsForValue().get("lock");.
//            if (ownerToken.equals(redisToken)) {
//                stringRedisTemplate.delete("lock");
//            }

                // 使用lua脚本删除锁，保证原子性
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), ownerToken);
            }

            return categoryEntities;
        } else {
            // 加锁失败，重试
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return listWithTreeFromDbWithRedisLocker();
        }
    }

    /**
     * 使用Redisson分布式锁
     *
     * @return
     */
    private List<CategoryEntity> listWithTreeFromDbWithRedisson() {
        // 获取分布式锁
        RLock lock = redissonClient.getLock("listWithTreeFromDbWithRedisson-lock");

        // 加锁
        lock.lock();

        List<CategoryEntity> categoryEntities = null;
        try {
            categoryEntities = getDataFromDbAndSetIntoRedis();
        } finally {
            lock.unlock();
        }

        return categoryEntities;
    }

    private List<CategoryEntity> getDataFromDbAndSetIntoRedis() {
        // 拿到锁之后，再查一遍redis
        String catalog = stringRedisTemplate.opsForValue().get("catalog");
        if (!StringUtils.isEmpty(catalog)) {
            return JSON.parseObject(catalog, new TypeReference<List<CategoryEntity>>() {
            });
        }

        // 查出所有分类
        List<CategoryEntity> entities = getBaseMapper().selectList(null);

        // 组装成树形结构
        // 获取一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == 0;
        }).map(menu -> {
            menu.setChildren(getChildren(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        // 查到的数据放入缓存，将对象序列化再存储
        log.debug("更新缓存");
        String jsonString = JSON.toJSONString(entities);
        stringRedisTemplate.opsForValue().set("catalog", jsonString, 1, TimeUnit.DAYS);

        return entities;
    }

    @Override
    public void removeCategoryByIds(List<Long> asList) {
        // TODO 检查当前删除的菜单是否被其他地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 更新数据库，如何保证数据库和缓存数据一致性？
     * 1. 双写模式：写完数据库，再写缓存，存在数据不一致问题，加锁保证但是粒度较大
     * 2. 失效模式：写数据库，再删缓存，
     *
     * 写操作较多或者对实时性要求非常高的数据，不需要增加缓存，直接读数据库更好
     *
     * 该接口的一致性解决方案：
     * 1. 缓存的所有数据都有过期时间，数据过期下一次触发主动更新
     * 2. 读写数据的时候，加上分布式读写锁，并采用失效模式
     *
     * @param categoryEntity
     */
    // @CacheEvict(value = {"category"},key = "'getLevel1Categorys'")
    @Transactional
    @Override
    public void updateCascade(CategoryEntity categoryEntity) {

        // 更新数据
        this.updateById(categoryEntity);
        categoryBrandRelationService.updateCategory(categoryEntity.getCatId(),categoryEntity.getName());
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);

        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }

        return paths;
    }
}