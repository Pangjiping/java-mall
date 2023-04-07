package org.epha.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.exception.BizException;
import org.epha.common.utils.PageUtils;
import org.epha.mall.ware.entity.WareSkuEntity;
import org.epha.mall.ware.vo.SkuHasStockVo;
import org.epha.mall.ware.vo.WareSkuLockRequest;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:40:35
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds);

    void orderLockStock(WareSkuLockRequest request) throws BizException;
}

