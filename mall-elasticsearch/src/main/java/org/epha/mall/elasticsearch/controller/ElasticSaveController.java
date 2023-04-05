package org.epha.mall.elasticsearch.controller;

import lombok.extern.slf4j.Slf4j;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.utils.R;
import org.epha.mall.elasticsearch.service.ProductSaveService;
import org.epha.mall.elasticsearch.to.SkuElasticsearchModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/search")
@RestController
@Slf4j
public class ElasticSaveController {

    @Resource
    ProductSaveService productSaveService;

    /**
     * 商品上架服务
     *
     * @param models
     * @return
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuElasticsearchModel> models) {

        boolean status = false;
        try {
            status = productSaveService.productStatusUp(models);
        } catch (Exception e) {
            log.error("商品上架接口异常: {}", e.getMessage());
        }

        if (status) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(),
                    BizCodeEnum.PRODUCT_UP_EXCEPTION.getMessage()
            );
        }
    }
}
