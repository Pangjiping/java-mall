package org.epha.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.utils.PageUtils;
import org.epha.mall.member.entity.MemberCollectSpuEntity;

import java.util.Map;

/**
 * 会员收藏的商品
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:33:02
 */
public interface MemberCollectSpuService extends IService<MemberCollectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

