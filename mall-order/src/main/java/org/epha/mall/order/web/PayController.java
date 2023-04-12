package org.epha.mall.order.web;

import com.alipay.api.AlipayApiException;
import org.epha.mall.order.configuration.AlipayTemplate;
import org.epha.mall.order.service.OrderService;
import org.epha.mall.order.vo.PayVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author pangjiping
 */
@RestController
public class PayController {

    @Resource
    AlipayTemplate alipayTemplate;

    @Resource
    OrderService orderService;

    @GetMapping(value = "/pay/{orderSn}",produces = "text/html")
    public String pay(@PathVariable("orderSn") String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPay(orderSn);
        String pay = alipayTemplate.pay(payVo);

        return pay;
    }

}
