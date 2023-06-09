package org.epha.mall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.ware.entity.WareInfoEntity;

/**
 * 仓库信息
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:40:35
 */
@Mapper
public interface WareInfoDao extends BaseMapper<WareInfoEntity> {
	
}
