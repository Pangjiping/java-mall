package org.epha.mall.coupon.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.coupon.dao.SeckillSessionDao;
import org.epha.mall.coupon.entity.SeckillSessionEntity;
import org.epha.mall.coupon.entity.SeckillSkuRelationEntity;
import org.epha.mall.coupon.service.SeckillSessionService;
import org.epha.mall.coupon.service.SeckillSkuRelationService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Resource
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> listThreeDaysSession() {

        // 计算最近三天
        String startTime = getStartTime();
        String endTime = getEndTime();

        List<SeckillSessionEntity> entities = this.list(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime, endTime));

        // 查出所有关联商品的信息
        if (entities != null && entities.size() > 0) {
            List<SeckillSessionEntity> sessions = entities.stream()
                    .map(session -> {
                        List<SeckillSkuRelationEntity> relationEntities = seckillSkuRelationService.list(
                                new QueryWrapper<SeckillSkuRelationEntity>()
                                        .eq("promotion_session_id", session.getId())
                        );
                        session.setRelationEntities(relationEntities);
                        return session;
                    })
                    .collect(Collectors.toList());

            return sessions;
        }

        return null;
    }

    private String getStartTime() {
        LocalTime min = LocalTime.MIN;
        return LocalDateTime.of(LocalDate.now(), min)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String getEndTime() {
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime max = LocalTime.MAX;
        return LocalDateTime.of(date, max)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}