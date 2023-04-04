package org.epha.mall.elasticsearch.service.impl;

import com.alibaba.fastjson.JSON;
import org.epha.mall.elasticsearch.service.MallSearchService;
import org.epha.mall.elasticsearch.vo.SearchParam;
import org.epha.mall.elasticsearch.vo.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MallSearchServiceImplTest {

    @Resource
    MallSearchService mallSearchService;

    @Test
    void search() throws IOException {
        SearchResult search = mallSearchService.search(generateSearchParam());
        String jsonString = JSON.toJSONString(search);
        System.out.println(jsonString);
    }

    private SearchParam generateSearchParam(){
        SearchParam searchParam = new SearchParam();

        searchParam.setKeyword("华为");
        searchParam.setCatalog3Id("225");
        searchParam.setBrandId(Arrays.asList(1L,9L));
        searchParam.setHasStock(0);
        searchParam.setSkuPrice("_6000");

        searchParam.setAttrs(Collections.singletonList("15_海思（Hisilicon）:以官网信息为准"));
        searchParam.setSort("skuPrice_desc");

        return searchParam;
    }
}