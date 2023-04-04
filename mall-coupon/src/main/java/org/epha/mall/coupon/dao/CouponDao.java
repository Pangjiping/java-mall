package org.epha.mall.coupon.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.coupon.entity.CouponEntity;

/**
 * 优惠券信息
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:27:06
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
