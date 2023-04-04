package org.epha.mall.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.coupon.entity.MemberPriceEntity;

/**
 * 商品会员价格
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:27:06
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
