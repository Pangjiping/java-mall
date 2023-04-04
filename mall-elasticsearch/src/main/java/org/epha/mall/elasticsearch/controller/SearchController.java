package org.epha.mall.elasticsearch.controller;

import lombok.extern.slf4j.Slf4j;
import org.epha.common.utils.R;
import org.epha.mall.elasticsearch.service.MallSearchService;
import org.epha.mall.elasticsearch.vo.SearchParam;
import org.epha.mall.elasticsearch.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author pangjiping
 */
@RestController
@Slf4j
public class SearchController {

    @Resource
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public R listPage(SearchParam param, Model model, HttpServletRequest request) throws IOException {
        param.set_queryString(request.getQueryString());

        // 根据页面传递过来的查询参数，去es中检索商品
        SearchResult searchResult = mallSearchService.search(param);

        // model.addAttribute("result", searchResult);

        return R.ok().put("data", searchResult);
    }
}
