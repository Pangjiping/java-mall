package org.epha.mall.order.web;

import org.epha.common.exception.BizException;
import org.epha.common.utils.R;
import org.epha.mall.order.service.OrderService;
import org.epha.mall.order.vo.OrderConfirmVo;
import org.epha.mall.order.vo.OrderSubmitRequest;
import org.epha.mall.order.vo.OrderSubmitResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author pangjiping
 */
@RestController
public class OrderWebController {

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

    /**
     * 提交订单
     */
    // @GlobalTransactional
    @PostMapping("/submit")
    public R submit(@RequestBody OrderSubmitRequest request) throws BizException, ExecutionException, InterruptedException {

        OrderSubmitResponse resp = orderService.submitOrder(request);

        return R.ok().setDate(resp);
    }
}
