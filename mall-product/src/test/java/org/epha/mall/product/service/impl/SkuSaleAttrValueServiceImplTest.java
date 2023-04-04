package org.epha.mall.product.service.impl;

import org.epha.mall.product.service.SkuSaleAttrValueService;
import org.epha.mall.product.vo.SkuItemSaleAttr;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class SkuSaleAttrValueServiceImplTest {

    @Resource
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Test
    public void getSaleAttrsBySpuIdTest(){
        List<SkuItemSaleAttr> attrs = skuSaleAttrValueService.getSaleAttrsBySpuId(13L);
        System.out.println(attrs);
    }

}