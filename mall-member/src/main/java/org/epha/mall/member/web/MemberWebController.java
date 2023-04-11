package org.epha.mall.member.web;

import org.epha.common.utils.R;
import org.epha.mall.member.feign.OrderFeignService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author pangjiping
 */
@Controller
public class MemberWebController {

    @Resource
    OrderFeignService orderFeignService;

    @GetMapping("/memberOrder.html")
    public R memberOrderPage(@RequestParam(value = "pageNumber", defaultValue = "1") String pageNumber) {

        // 查出当前登录用户的所有订单列表数据
        HashMap<String, Object> params = new HashMap<>();
        params.put("page", pageNumber);

        return orderFeignService.listWithItems(params);
    }
}
