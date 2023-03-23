package org.epha.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.utils.PageUtils;
import org.epha.mall.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 19:35:03
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

