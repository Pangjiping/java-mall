package org.epha.mall.ware;

import org.epha.mall.ware.entity.WareInfoEntity;
import org.epha.mall.ware.service.WareInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class MallWareApplicationTests {

    @Resource
    WareInfoService wareInfoService;

    @Test
    void contextLoads() {
        WareInfoEntity infoEntity = new WareInfoEntity();
        infoEntity.setName("lll");
        wareInfoService.save(infoEntity);
    }

}
