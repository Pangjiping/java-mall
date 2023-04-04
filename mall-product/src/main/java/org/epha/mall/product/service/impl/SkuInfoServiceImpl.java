package org.epha.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.product.dao.SkuInfoDao;
import org.epha.mall.product.entity.SkuImagesEntity;
import org.epha.mall.product.entity.SkuInfoEntity;
import org.epha.mall.product.entity.SpuInfoDescEntity;
import org.epha.mall.product.service.*;
import org.epha.mall.product.vo.SkuItemSaleAttr;
import org.epha.mall.product.vo.SkuItemVo;
import org.epha.mall.product.vo.SpuItemAttrGroup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author pangjiping
 */
@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    SkuImagesService skuImagesService;

    @Resource
    SpuInfoDescService spuInfoDescService;

    @Resource
    AttrGroupService attrGroupService;

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {

        return this.list(
                new QueryWrapper<SkuInfoEntity>()
                        .eq("spu_id", spuId)
        );
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        // sku的基本信息 pms_sku_info
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfoEntity = this.getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPoolExecutor);

        // 获取spu的销售属性组合
        CompletableFuture<Void> skuSaleAttrFuture = infoFuture.thenAcceptAsync(res -> {
            List<SkuItemSaleAttr> skuItemSaleAttrs = skuSaleAttrValueService.getSaleAttrsBySpuId(res.getSpuId());
            skuItemVo.setSaleAttrs(skuItemSaleAttrs);
        }, threadPoolExecutor);

        // 获取spu的介绍
        CompletableFuture<Void> spuInfoFuture = infoFuture.thenAcceptAsync(res -> {
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDescribe(spuInfoDescEntity);
        }, threadPoolExecutor);

        // 获取spu的规格参数信息
        CompletableFuture<Void> attrGroupFuture = infoFuture.thenAcceptAsync(res -> {
            List<SpuItemAttrGroup> attrGroups = attrGroupService.getAttrGroupWithAttrsBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(attrGroups);
        }, threadPoolExecutor);

        // sku的图片信息 pms_sku_images
        CompletableFuture<Void> imageFuture = CompletableFuture.runAsync(() -> {
            List<SkuImagesEntity> imagesEntities = skuImagesService.getImageBySkuId(skuId);
            skuItemVo.setImages(imagesEntities);
        }, threadPoolExecutor);


        // 等待所有异步任务结束
        CompletableFuture.allOf(
                skuSaleAttrFuture,
                spuInfoFuture,
                attrGroupFuture,
                imageFuture
        ).get();


        return skuItemVo;
    }

}