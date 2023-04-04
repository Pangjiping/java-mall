package org.epha.mall.product.web;

import org.epha.common.utils.R;
import org.epha.mall.product.service.SkuInfoService;
import org.epha.mall.product.vo.SkuItemVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * @author pangjiping
 */
@RestController
public class ItemController {

    @Resource
    SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public R skuItem(@PathVariable("skuId") Long skuId) throws ExecutionException, InterruptedException {

        SkuItemVo item = skuInfoService.item(skuId);

        return R.ok().setDate(item);
    }
}
