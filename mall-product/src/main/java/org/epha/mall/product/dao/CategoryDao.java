package org.epha.mall.product.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.product.entity.CategoryEntity;

/**
 * 商品三级分类
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 19:35:03
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
