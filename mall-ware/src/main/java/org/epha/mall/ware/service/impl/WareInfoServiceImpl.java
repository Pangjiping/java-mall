package org.epha.mall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.common.utils.R;
import org.epha.mall.ware.dao.WareInfoDao;
import org.epha.mall.ware.entity.WareInfoEntity;
import org.epha.mall.ware.feign.MemberFeignService;
import org.epha.mall.ware.service.WareInfoService;
import org.epha.mall.ware.vo.MemberAddressVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Resource
    MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public BigDecimal getFare(Long addrId) {

        R r = memberFeignService.getAddressInfo(addrId);
        if (r.getCode() == 0) {
            MemberAddressVo address = r.getData(new TypeReference<>() {
            });

            if (address!=null){
                String phone = address.getPhone();
                String s = phone.substring(phone.length() - 1, phone.length());
                return new BigDecimal(s);
            }
        }

        return BigDecimal.ZERO;
    }

}