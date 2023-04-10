package org.epha.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.utils.PageUtils;
import org.epha.mall.order.entity.MqMessageEntity;

import java.util.Map;

/**
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-04-09 11:39:27
 */
public interface MqMessageService extends IService<MqMessageEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void createOrderCreateMessageRecord(String uuid, String content);

    void updateMessageRecord(String uuid, Integer status);

    void updateMessageRecord(String uuid, Integer status,String errMessage);

    void updateMessageRecord(String uuid, Integer status,Integer retry);

    void createOrderCloseMessageRecord(String uuid, String content);
}

