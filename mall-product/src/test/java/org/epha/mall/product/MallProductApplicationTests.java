package org.epha.mall.product;

import org.epha.mall.product.entity.BrandEntity;
import org.epha.mall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MallProductApplicationTests {

    @Resource
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("test");
        brandEntity.setName("华为");
        boolean b = brandService.save(brandEntity);
        System.out.println("保存成功...");
    }

}
