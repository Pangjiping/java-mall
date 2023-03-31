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
import org.epha.mall.product.service.CategoryService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
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
            List<CategoryEntity> categoryEntities = listWithTreeFromDbWithLocalLocker();

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
    private List<CategoryEntity> listWithTreeFromDbWithLocalLocker() {

        synchronized (this) {

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