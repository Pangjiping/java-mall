package org.epha.mall.member.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.epha.mall.member.entity.MemberCollectSubjectEntity;

/**
 * 会员收藏的专题活动
 * 
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:33:02
 */
@Mapper
public interface MemberCollectSubjectDao extends BaseMapper<MemberCollectSubjectEntity> {
	
}
