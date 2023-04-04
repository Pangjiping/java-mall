package org.epha.mall.product.service.impl;

import org.epha.mall.product.service.AttrGroupService;
import org.epha.mall.product.vo.SpuItemAttrGroup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class AttrGroupServiceImplTest {

    @Resource
    AttrGroupService attrGroupService;

    @Test
    public void getAttrGroupWithAttrsBySpuIdTest(){
        List<SpuItemAttrGroup> groups = attrGroupService.getAttrGroupWithAttrsBySpuId(13L, 225L);
        System.out.println(groups);
    }

}