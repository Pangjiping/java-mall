package org.epha.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.epha.mall.product.entity.CategoryBrandRelationEntity;

/**
 * 品牌分类关联
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 19:35:03
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
