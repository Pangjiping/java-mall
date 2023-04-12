package org.epha.mall.seckill.controller;

import org.epha.common.exception.BizException;
import org.epha.common.utils.R;
import org.epha.mall.seckill.service.SeckillService;
import org.epha.mall.seckill.vo.SeckillSkuRelation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author pangjiping
 */
@RestController
public class WebController {

    @Resource
    SeckillService seckillService;

    /**
     * 查询当前参与秒杀的商品
     */
    @GetMapping("/currentseckill/skus")
    public R listCurrentSkus() {

        List<SeckillSkuRelation> skus = seckillService.listCurrentSkus();

        return R.ok().setDate(skus);
    }

    @GetMapping("/kill")
    public R seckill(@RequestParam("killId") String killId,
                     @RequestParam("randomCode") String randomCode,
                     @RequestParam("num") Integer number) throws BizException {

        // 拦截器登录验证
        // 秒杀逻辑
        String orderSn = seckillService.kill(killId, randomCode, number);

        return R.ok().setDate(orderSn);
    }

}
