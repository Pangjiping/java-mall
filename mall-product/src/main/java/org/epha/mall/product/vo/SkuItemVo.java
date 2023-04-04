package org.epha.mall.product.vo;

import lombok.Data;
import org.epha.mall.product.entity.SkuImagesEntity;
import org.epha.mall.product.entity.SkuInfoEntity;
import org.epha.mall.product.entity.SpuInfoDescEntity;

import java.util.List;

/**
 * @author pangjiping
 */
@Data
public class SkuItemVo {

    /**
     * sku的基本信息 pms_sku_info
     */
    private SkuInfoEntity info;

    /**
     * sku的图片信息 pms_sku_images
     */
    private List<SkuImagesEntity> images;

    /**
     * 获取spu的销售属性组合
     */
    private List<SkuItemSaleAttr> saleAttrs;

    /**
     * 获取spu的介绍
     */
    private SpuInfoDescEntity describe;

    /**
     * 获取spu的规格参数信息
     */
    private List<SpuItemAttrGroup> groupAttrs;
}
