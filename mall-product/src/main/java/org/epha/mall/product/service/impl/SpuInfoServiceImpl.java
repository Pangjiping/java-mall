package org.epha.mall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.constant.ProductConstant;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.common.utils.R;
import org.epha.mall.product.dao.SpuInfoDao;
import org.epha.mall.product.entity.*;
import org.epha.mall.product.feign.ElasticsearchFeignService;
import org.epha.mall.product.feign.WareFeignService;
import org.epha.mall.product.service.*;
import org.epha.mall.product.to.SkuElasticsearchModel;
import org.epha.mall.product.vo.SkuHasStockVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    SkuInfoService skuInfoService;

    @Resource
    BrandService brandService;

    @Resource
    CategoryService categoryService;

    @Resource
    ProductAttrValueService productAttrValueService;

    @Resource
    AttrService attrService;

    @Resource
    WareFeignService wareFeignService;

    @Resource
    ElasticsearchFeignService elasticsearchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        // 1. 查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIds = skuInfoEntities.stream().map(skuInfoEntity -> {
            return skuInfoEntity.getSkuId();
        }).collect(Collectors.toList());

        // 2. 查询当前sku的所有可被用来检索的规格属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrListforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(productAttrValueEntity -> {
            return productAttrValueEntity.getAttrId();
        }).collect(Collectors.toList());

        List<Long> searchAttrs = attrService.selectSearchAttrs(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrs);

        List<SkuElasticsearchModel.Attr> attrs = productAttrValueEntities.stream().filter(productAttrValueEntity -> {
            return idSet.contains(productAttrValueEntity.getAttrId());
        }).map(item -> {
            SkuElasticsearchModel.Attr attr = new SkuElasticsearchModel.Attr();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());

        // 发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIds);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<>() {};

            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(
                    SkuHasStockVo::getSkuId,
                    item -> item.getHasStock()
            ));
        } catch (Exception e) {
            log.error("库存服务查询异常: {}", e);
        }


        // 3. 封装每个sku的信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuElasticsearchModel> skuElasticsearchModelList = skuInfoEntities.stream().map(skuInfoEntity -> {
            SkuElasticsearchModel model = new SkuElasticsearchModel();
            BeanUtils.copyProperties(skuInfoEntity, model);

            // 处理特殊属性
            model.setSkuPrice(skuInfoEntity.getPrice());
            model.setSkuImg(skuInfoEntity.getSkuDefaultImg());

            // TODO: 热度评分
            model.setHotScore(0L);

            // 设置库存信息
            if (finalStockMap == null) {
                model.setHasStock(false);
            } else {
                model.setHasStock(finalStockMap.get(skuInfoEntity.getSkuId()));
            }

            // 查询品牌和分类的名字信息
            BrandEntity brandEntity = brandService.getById(model.getBrandId());
            model.setBrandName(brandEntity.getName());
            model.setBrandImg(brandEntity.getLogo());

            CategoryEntity categoryEntity = categoryService.getById(model.getCatalogId());
            model.setCatalogName(categoryEntity.getName());

            // 设置检索属性
            model.setAttrs(attrs);

            return model;

        }).collect(Collectors.toList());

        // 将数据发送给es服务保存
        R r = elasticsearchFeignService.productStatusUp(skuElasticsearchModelList);
        if (r.getCode() == 0) {
            // 远程调用成功，修改spu状态
            getBaseMapper().updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // TODO 调用失败，接口幂等性？
            log.error("商品上架失败");
        }
    }

}