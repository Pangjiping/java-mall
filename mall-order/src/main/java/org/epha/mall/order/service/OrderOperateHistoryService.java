package org.epha.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.utils.PageUtils;
import org.epha.mall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:37:46
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

