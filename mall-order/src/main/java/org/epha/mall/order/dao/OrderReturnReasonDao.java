package org.epha.mall.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.order.entity.OrderReturnReasonEntity;

/**
 * 退货原因
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:37:46
 */
@Mapper
public interface OrderReturnReasonDao extends BaseMapper<OrderReturnReasonEntity> {
	
}
