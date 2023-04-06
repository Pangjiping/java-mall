package org.epha.mall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.common.utils.R;
import org.epha.mall.order.dao.OrderDao;
import org.epha.mall.order.entity.OrderEntity;
import org.epha.mall.order.feign.CartFeignService;
import org.epha.mall.order.feign.MemberFeignService;
import org.epha.mall.order.feign.WareFeignService;
import org.epha.mall.order.interceptor.LoginUserInterceptor;
import org.epha.mall.order.service.OrderService;
import org.epha.mall.order.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


/**
 * @author pangjiping
 */
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Resource
    MemberFeignService memberFeignService;

    @Resource
    CartFeignService cartFeignService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Resource
    WareFeignService wareFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {

        LoginUser user = LoginUserInterceptor.threadLocal.get();

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 保留一个默认地址id
        final Long[] defaultAddrId = {0L};

        // 拿到当前线程threadLocal中的request信息，共享给异步feign线程
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 查询收货地址列表
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {

            // 把之前的request信息放到这个异步线程里面，确保cookie存在
            RequestContextHolder.setRequestAttributes(requestAttributes);

            R r = memberFeignService.getAddress(user.getId());
            if (r.getCode() == 0) {
                List<MemberAddressVo> address = r.getData(new TypeReference<>() {
                });
                confirmVo.setAddress(address);

                if (address != null && address.size() > 0) {
                    // 拿到默认地址id
                    Optional<MemberAddressVo> defaultAddr = address.stream()
                            .filter(memberAddressVo ->
                                    memberAddressVo.getDefaultStatus() == 1
                            )
                            .findFirst();

                    defaultAddr.ifPresent(memberAddressVo ->
                            defaultAddrId[0] = memberAddressVo.getId()
                    );
                }

            }
        }, threadPoolExecutor);

        // 查询购物车所有选中的购物项
        CompletableFuture<Void> itemFuture = CompletableFuture.runAsync(() -> {

            // 把之前的request信息放到这个异步线程里面，确保cookie存在
            RequestContextHolder.setRequestAttributes(requestAttributes);

            R r = cartFeignService.getCheckedItems();
            if (r.getCode() == 0) {
                List<OrderItemVo> items = r.getData(new TypeReference<>() {
                });
                confirmVo.setItems(items);
            }
        }, threadPoolExecutor)
                .thenRun(() -> {

                    // 查询库存信息
                    List<OrderItemVo> items = confirmVo.getItems();
                    List<Long> skuIds = items.stream()
                            .map(OrderItemVo::getSkuId)
                            .collect(Collectors.toList());

                    R r = wareFeignService.getSkusHasStock(skuIds);
                    if (r.getCode() == 0) {
                        List<SkuHasStockVo> hasStocks = r.getData(new TypeReference<>() {
                        });

                        if (hasStocks != null && hasStocks.size() > 0) {
                            Map<Long, Boolean> hasStockMap = hasStocks.stream()
                                    .collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                            confirmVo.setHasStockMap(hasStockMap);
                        }
                    }

                });


        // 查询用户积分
        confirmVo.setIntegration(user.getIntegration());

        // TODO 防重复令牌


        CompletableFuture.allOf(addressFuture, itemFuture).get();

        // 设置运费信息，简单设置，运费 = 有货的商品数量*运费单价
        // 获取运费信息
        R r = wareFeignService.getFare(defaultAddrId[0]);
        if (r.getCode() == 0) {
            BigDecimal fare = r.getData(new TypeReference<>() {
            });

            if (confirmVo.getItems() != null && confirmVo.getItems().size() > 0) {

                final BigDecimal[] totalFare = {BigDecimal.ZERO};

                confirmVo.getItems().forEach(orderItemVo -> {
                    // 如果有库存，计算运费
                    if (confirmVo.getHasStockMap().get(orderItemVo.getSkuId())) {
                        totalFare[0] = totalFare[0].add(fare.multiply(new BigDecimal(orderItemVo.getCount().toString())));
                    }
                });

                confirmVo.setFare(totalFare[0]);
            }

        }

        return confirmVo;
    }

}