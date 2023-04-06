package org.epha.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.epha.mall.product.entity.SkuSaleAttrValueEntity;
import org.epha.mall.product.vo.SkuItemSaleAttr;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 19:35:03
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemSaleAttr> getSaleAttrsBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(@Param("skuId") Long skuId);
}
