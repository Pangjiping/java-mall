package org.epha.mall.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.order.entity.MqMessageEntity;

/**
 * 
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-04-09 11:39:27
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
