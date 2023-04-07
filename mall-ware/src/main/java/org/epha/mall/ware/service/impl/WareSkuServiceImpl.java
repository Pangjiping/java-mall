package org.epha.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.ware.dao.WareSkuDao;
import org.epha.mall.ware.entity.WareSkuEntity;
import org.epha.mall.ware.service.WareSkuService;
import org.epha.mall.ware.vo.OrderItemVo;
import org.epha.mall.ware.vo.SkuHasStockVo;
import org.epha.mall.ware.vo.WareSkuLockRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @author pangjiping
 */
@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(skuId -> {
            SkuHasStockVo stockVo = new SkuHasStockVo();

            // 查询sku库存
            Long count = getBaseMapper().getSkuStock(skuId);
            if (count == null) {
                // 如果查不到库存信息，先默认有库存
                stockVo.setHasStock(true);
            } else {
                stockVo.setHasStock(count > 0);
            }

            stockVo.setSkuId(skuId);

            return stockVo;
        }).collect(Collectors.toList());

        return skuHasStockVos;
    }

    /**
     * 为某个订单锁定库存
     * 涉及多个商品的库存，封装成事务操作，快速失败，失败回滚
     */
    @Transactional(rollbackFor = BizException.class)
    @Override
    public void orderLockStock(WareSkuLockRequest request) throws BizException {

        // 找到每个商品在哪个仓库有库存
        List<OrderItemVo> locks = request.getLocks();
        List<SkuWareHasStock> skuWareHasStocks = locks.stream()
                .map(item -> {

                    SkuWareHasStock skuWareHasStock = new SkuWareHasStock();
                    skuWareHasStock.setSkuId(item.getSkuId());
                    skuWareHasStock.setNum(item.getCount());

                    // 查询商品在哪里有库存
                    List<Long> wareIds = getBaseMapper().listWareIdsHasStock(item.getSkuId());
                    skuWareHasStock.setWareIds(wareIds);

                    return skuWareHasStock;
                })
                .collect(Collectors.toList());

        // 锁定库存
        for (SkuWareHasStock skuWareHasStock : skuWareHasStocks) {

            boolean skuStockLocked = false;

            Long skuId = skuWareHasStock.getSkuId();
            List<Long> wareIds = skuWareHasStock.getWareIds();

            // 没有仓库信息，快速失败
            if (wareIds == null || wareIds.size() == 0) {
                throw new BizException(BizCodeEnum.EMPTY_STOCK_EXCEPTION);
            }

            for (Long wareId : wareIds) {
                Long count = getBaseMapper().lockSkuStock(skuId, wareId, skuWareHasStock.getNum());
                if (count == 1) {
                    // 锁定成功，跳出循环
                    skuStockLocked = true;
                    break;
                }
            }

            // 如果当前商品没有锁住库存（库存不足），抛出异常，快速失败
            if (!skuStockLocked) {
                throw new BizException(BizCodeEnum.EMPTY_STOCK_EXCEPTION);
            }
        }
    }

    @Data
    static class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareIds;
    }

}