package org.epha.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.epha.mall.product.entity.AttrGroupEntity;
import org.epha.mall.product.vo.SpuItemAttrGroup;

import java.util.List;

/**
 * 属性分组
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 19:35:03
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroup> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
