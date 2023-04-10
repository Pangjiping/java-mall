package org.epha.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.utils.PageUtils;
import org.epha.mall.ware.entity.MqMessageEntity;

import java.util.Map;

/**
 * 
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-04-09 11:35:13
 */
public interface MqMessageService extends IService<MqMessageEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

