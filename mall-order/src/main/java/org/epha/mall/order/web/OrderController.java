package org.epha.mall.order.web;

import org.epha.common.utils.R;
import org.epha.mall.order.service.OrderService;
import org.epha.mall.order.vo.OrderConfirmVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author pangjiping
 */
@RestController
public class OrderController {

    @Resource
    OrderService orderService;

    /**
     * 确认订单页面数据
     */
    @GetMapping("/confirm")
    public R confirm() throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();

        return R.ok().setDate(orderConfirmVo);
    }
}
