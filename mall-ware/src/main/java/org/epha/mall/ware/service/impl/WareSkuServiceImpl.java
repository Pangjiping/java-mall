package org.epha.mall.ware.service.impl;

import org.epha.mall.ware.vo.SkuHasStockVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;

import org.epha.mall.ware.dao.WareSkuDao;
import org.epha.mall.ware.entity.WareSkuEntity;
import org.epha.mall.ware.service.WareSkuService;


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

}