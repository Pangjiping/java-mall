package org.epha.mall.seckill.service;

import org.epha.common.exception.BizException;
import org.epha.mall.seckill.vo.SeckillSkuRelation;

import java.util.List;

public interface SeckillService {
    void upSeckillSku();

    List<SeckillSkuRelation> listCurrentSkus();

    String kill(String killId, String randomCode, Integer number) throws BizException;
}
