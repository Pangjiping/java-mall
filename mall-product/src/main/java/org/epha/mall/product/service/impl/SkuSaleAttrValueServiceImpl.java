package org.epha.mall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.product.dao.SkuSaleAttrValueDao;
import org.epha.mall.product.entity.SkuSaleAttrValueEntity;
import org.epha.mall.product.service.SkuSaleAttrValueService;
import org.epha.mall.product.vo.SkuItemSaleAttr;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemSaleAttr> getSaleAttrsBySpuId(Long spuId) {
        return getBaseMapper().getSaleAttrsBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrValuesAsStringList(Long skuId) {
        return getBaseMapper().getSkuSaleAttrValuesAsStringList(skuId);
    }

}