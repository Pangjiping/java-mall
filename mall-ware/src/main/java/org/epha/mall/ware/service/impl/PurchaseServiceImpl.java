package org.epha.mall.ware.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.ware.dao.PurchaseDao;
import org.epha.mall.ware.entity.PurchaseEntity;
import org.epha.mall.ware.service.PurchaseService;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

}