package org.epha.mall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.constant.OrderConstant;
import org.epha.common.enumm.OrderStatusEnum;
import org.epha.common.exception.BizCodeEnum;
import org.epha.common.exception.BizException;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.common.utils.R;
import org.epha.mall.order.dao.OrderDao;
import org.epha.mall.order.entity.OrderEntity;
import org.epha.mall.order.entity.OrderItemEntity;
import org.epha.mall.order.feign.CartFeignService;
import org.epha.mall.order.feign.MemberFeignService;
import org.epha.mall.order.feign.ProductFeignService;
import org.epha.mall.order.feign.WareFeignService;
import org.epha.mall.order.interceptor.LoginUserInterceptor;
import org.epha.mall.order.service.MqMessageService;
import org.epha.mall.order.service.OrderItemService;
import org.epha.mall.order.service.OrderService;
import org.epha.mall.order.to.CreatedOrder;
import org.epha.mall.order.vo.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @author pangjiping
 */
@Slf4j
@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private static final String ANTI_REENTRANT_SCRIPT = "if redis.call('get', KEYS[1]) == ARGV[1] " +
            "then return redis.call('del', KEYS[1]) " +
            "else return 0 end";

    private final ThreadLocal<OrderSubmitRequest> orderSubmitRequest = new ThreadLocal<>();

    private static final String ORDER_CREATE_PREFIX = "cre_o:";
    private static final String ORDER_RELEASE_PREFIX = "rel_o:";

    @Resource
    MemberFeignService memberFeignService;

    @Resource
    CartFeignService cartFeignService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Resource
    WareFeignService wareFeignService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    OrderItemService orderItemService;

    @Resource
    RabbitTemplate rabbitTemplate;

    @Resource
    MqMessageService mqMessageService;

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

        // 保留默认地址运费
        final BigDecimal[] defaultFare = {BigDecimal.ZERO};

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
            }
        }, threadPoolExecutor)
                .thenRun(() -> {
                    if (confirmVo.getAddress() != null && confirmVo.getAddress().size() > 0) {
                        // 拿到默认地址id
                        final Long[] defaultAddrId = {0L};
                        confirmVo.getAddress()
                                .forEach(memberAddressVo -> {
                                    if (memberAddressVo.getDefaultStatus() == 1) {
                                        defaultAddrId[0] = memberAddressVo.getId();
                                    }
                                });

                        // 拿到运费
                        R r = wareFeignService.getFare(defaultAddrId[0]);
                        if (r.getCode() == 0) {
                            defaultFare[0] = r.getData(new TypeReference<>() {
                            });
                        }
                    }
                });

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

        // TODO 防重复令牌 返回值放一份，redis放一份
        String token = UUID.randomUUID().toString().replace("-", "");
        confirmVo.setOrderToken(token);
        stringRedisTemplate.opsForValue().set(
                OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId().toString(),
                token,
                30,
                TimeUnit.MINUTES);

        CompletableFuture.allOf(addressFuture, itemFuture).get();

        // 设置运费信息，简单设置，运费 = 有货的商品数量*运费单价
        if (confirmVo.getItems() != null && confirmVo.getItems().size() > 0) {

            final BigDecimal[] totalFare = {BigDecimal.ZERO};

            confirmVo.getItems().forEach(orderItemVo -> {
                // 如果有库存，计算运费
                if (confirmVo.getHasStockMap().get(orderItemVo.getSkuId())) {
                    totalFare[0] = totalFare[0].add(defaultFare[0].multiply(new BigDecimal(orderItemVo.getCount().toString())));
                }
            });

            confirmVo.setFare(totalFare[0]);
        }

        return confirmVo;
    }

    /**
     * 提交订单：验令牌、创建订单、验价格、锁库存...
     */
    @Transactional
    @Override
    public OrderSubmitResponse submitOrder(OrderSubmitRequest request) throws BizException, ExecutionException, InterruptedException {

        orderSubmitRequest.set(request);

        // 防止重入
        antiReentrancy();

        OrderSubmitResponse response = new OrderSubmitResponse();

        // 创建订单
        CreatedOrder order = createOrder();

        // 验价失败，返回异常
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = request.getPayPrice();
        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) >= 0.01) {
            throw new BizException(BizCodeEnum.PRICE_MISMATCH_EXCEPTION);
        }

        // 订单数据保存到数据库
        saveOrder(order);

        // 库存锁定，只要有异常，回滚订单数据，事务保障！
        WareSkuLockRequest lockRequest = new WareSkuLockRequest();
        lockRequest.setOrderSn(order.getOrder().getOrderSn());
        List<OrderItemVo> orderItems = order.getOrderItems().stream()
                .map(orderItemEntity -> {
                    OrderItemVo itemVo = new OrderItemVo();
                    itemVo.setSkuId(orderItemEntity.getSkuId());
                    itemVo.setCount(orderItemEntity.getSkuQuantity());
                    return itemVo;
                })
                .collect(Collectors.toList());
        lockRequest.setLocks(orderItems);

        R r = wareFeignService.orderLockStock(lockRequest);
        if (r.getCode() != 0) {
            // TODO 锁定失败怎么办？
            // 直接抛异常，让本机事务 saveOrder(order); 回滚
            log.error("远程调用库存失败：{}", r.getErrorMessage());
            throw new BizException(BizCodeEnum.EMPTY_STOCK_EXCEPTION);
        }

        // 订单创建成功，发送消息给MQ
        sendOrderCreateMessage(order.getOrder());

        response.setOrderEntity(order.getOrder());

        orderSubmitRequest.remove();

        return response;
    }

    /**
     * 发送订单创建消息
     */
    private void sendOrderCreateMessage(OrderEntity order) {

        String uuid = UUID.randomUUID().toString().replace("-", "");
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(uuid);
        properties.setContentType("text/plain");
        properties.setContentEncoding("utf-8");

        String content = JSON.toJSONString(order);
        Message message = new Message(content.getBytes(StandardCharsets.UTF_8), properties);
        CorrelationData correlationData = new CorrelationData(uuid);

        try {
            // 发送消息
            rabbitTemplate.convertAndSend(
                    OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                    OrderConstant.MQ_ROUTING_KEY_ORDER_CREATE,
                    message,
                    correlationData
            );
        } catch (Exception e) {
            log.error("消息{} 发送失败: {}", uuid, e.getMessage());
        } finally {
            mqMessageService.createOrderCreateMessageRecord(uuid, content);
        }
    }

    @Override
    public Integer getOrderStatusByOrderSn(String orderSn) {
        OrderEntity order = this.getOne(
                new QueryWrapper<OrderEntity>()
                        .eq("order_sn", orderSn)
        );
        if (order == null) {
            return OrderStatusEnum.CANCLED.getCode();
        }
        return order.getStatus();
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {
        // 查询订单的最新状态
        OrderEntity order = this.getById(orderEntity.getId());

        // 什么情况下需要关单
        if (order != null && order.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode())) {
            // 关闭订单
            OrderEntity update = new OrderEntity();
            update.setStatus(OrderStatusEnum.CANCLED.getCode());
            update.setId(orderEntity.getId());

            this.updateById(update);

            // 只要订单解锁成功，再给MQ发个消息
            // TODO 保证消息百分百发出去
            sendOrderCloseMessage(order);
        }
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        PayVo payVo = new PayVo();
        OrderEntity order = this.getOne(
                new QueryWrapper<OrderEntity>()
                        .eq("order_sn", orderSn)
        );

        payVo.setTotal_amount(order.getPayAmount().setScale(2, RoundingMode.UP).toString());
        payVo.setOut_trade_no(orderSn);
        payVo.setSubject("收银");
        return payVo;
    }

    @Override
    public PageUtils listWithItem(Map<String, Object> params) {

        LoginUser user = LoginUserInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
                        .eq("member_id", user.getId())
                        .orderByDesc("id")
        );

        List<OrderEntity> orderEntities = page.getRecords().stream()
                .map(orderEntity -> {
                    List<OrderItemEntity> itemEntities = orderItemService.list(
                            new QueryWrapper<OrderItemEntity>()
                                    .eq("order_sn", orderEntity.getOrderSn())
                    );
                    orderEntity.setOrderItems(itemEntities);
                    return orderEntity;
                })
                .collect(Collectors.toList());

        page.setRecords(orderEntities);

        return new PageUtils(page);
    }

    private void sendOrderCloseMessage(OrderEntity order) {

        String uuid = UUID.randomUUID().toString().replace("-", "");
        MessageProperties properties = new MessageProperties();
        properties.setMessageId(uuid);
        properties.setContentType("text/plain");
        properties.setContentEncoding("utf-8");

        String content = JSON.toJSONString(order);
        Message message = new Message(content.getBytes(StandardCharsets.UTF_8), properties);
        CorrelationData correlationData = new CorrelationData(uuid);

        try {
            rabbitTemplate.convertAndSend(
                    OrderConstant.MQ_EXCHANGE_ORDER_EVENT,
                    "order.release.other",
                    message,
                    correlationData
            );
        } catch (Exception e) {
            log.error("消息{} 发送失败: {}", uuid, e.getMessage());
        } finally {
            mqMessageService.createOrderCloseMessageRecord(uuid, content);
        }

    }

    /**
     * 保存订单信息到数据库
     */
    private void saveOrder(CreatedOrder order) {
        // 保存订单
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.save(orderEntity);

        // 保存orderItem
        List<OrderItemEntity> orderItemEntities = order.getOrderItems();
        orderItemService.saveBatch(orderItemEntities);
    }

    /**
     * 防止订单提交请求重入，直接抛异常
     * 原子性令牌查询和删除
     */
    private void antiReentrancy() throws BizException {

        LoginUser user = LoginUserInterceptor.threadLocal.get();
        OrderSubmitRequest request = this.orderSubmitRequest.get();

        // 返回0代表令牌校验失败
        Long res = stringRedisTemplate.execute(new DefaultRedisScript<Long>(ANTI_REENTRANT_SCRIPT, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + user.getId()),
                request.getOrderToken());
        if (res != null && res == 1L) {
        } else {
            throw new BizException(BizCodeEnum.REPEATE_ORDER_SUNMIT_EXCEPTION);
        }
    }

    /**
     * 创建订单信息
     */
    private CreatedOrder createOrder() throws ExecutionException, InterruptedException {

        OrderSubmitRequest request = this.orderSubmitRequest.get();
        LoginUser user = LoginUserInterceptor.threadLocal.get();

        // 拿到当前线程threadLocal中的request信息，共享给异步feign线程
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CreatedOrder order = new CreatedOrder();
        OrderEntity orderEntity = new OrderEntity();

        // 设置会员信息
        orderEntity.setMemberId(user.getId());
        orderEntity.setMemberUsername(user.getUsername());

        // 生成一个订单号
        String id = IdWorker.getTimeId();
        String orderId = id.substring(id.length() - 20);
        orderEntity.setOrderSn(orderId);

        // 获取到所有的订单项
        CompletableFuture<Void> orderItemFuture = CompletableFuture.runAsync(() -> {

            // 把之前的request信息放到这个异步线程里面，确保cookie存在
            RequestContextHolder.setRequestAttributes(requestAttributes);

            R r2 = cartFeignService.getCheckedItems();
            if (r2.getCode() == 0) {
                List<OrderItemVo> items = r2.getData(new TypeReference<>() {
                });
                if (items != null && items.size() > 0) {
                    // 构建OrderItemEntity
                    List<OrderItemEntity> orderItemEntities = buildOrderItemEntities(items, orderId);
                    order.setOrderItems(orderItemEntities);

                    BigDecimal total = BigDecimal.ZERO;
                    for (OrderItemEntity orderItem : orderItemEntities) {
                        total = total.add(orderItem.getRealAmount());
                    }

                    // 订单总额，叠加每一个购物项的金额
                    orderEntity.setTotalAmount(total);
                }
            }
        }, threadPoolExecutor);

        // 获取邮费信息
        CompletableFuture<Void> fareFuture = CompletableFuture.runAsync(() -> {

            // 把之前的request信息放到这个异步线程里面，确保cookie存在
            RequestContextHolder.setRequestAttributes(requestAttributes);

            R r = wareFeignService.getFare(request.getAddressId());
            if (r.getCode() == 0) {
                BigDecimal fare = r.getData(new TypeReference<>() {
                });

                orderEntity.setFreightAmount(fare);
            }
        }, threadPoolExecutor);

        // 根据地址Id获取收货地址信息
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {

            // 把之前的request信息放到这个异步线程里面，确保cookie存在
            RequestContextHolder.setRequestAttributes(requestAttributes);

            R r1 = memberFeignService.info(request.getAddressId());
            if (r1.getCode() == 0) {
                MemberAddressVo address = r1.getData(new TypeReference<>() {
                });

                // 设置收货信息
                orderEntity.setReceiverCity(address.getCity());
                orderEntity.setReceiverDetailAddress(address.getDetailAddress());
                orderEntity.setReceiverName(address.getName());
                orderEntity.setReceiverPhone(address.getPhone());
                orderEntity.setReceiverPostCode(address.getPostCode());
                orderEntity.setReceiverProvince(address.getProvince());
                orderEntity.setReceiverRegion(address.getRegion());
            }
        }, threadPoolExecutor);

        CompletableFuture.allOf(orderItemFuture, fareFuture, addressFuture).get();

        // 设置应付总额：订单总额+运费
        orderEntity.setPayAmount(orderEntity.getTotalAmount().add(orderEntity.getFreightAmount()));

        // 设置订单状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        orderEntity.setDeleteStatus(0); // 未删除

        order.setOrder(orderEntity);

        return order;
    }

    private List<OrderItemEntity> buildOrderItemEntities(List<OrderItemVo> items, String orderSn) {

        List<OrderItemEntity> orderItemEntityList = items.stream()
                .map(item -> {

                    // TODO: 使用一个批量远程查询接口，不要在for里面查
                    OrderItemEntity orderItemEntity = buildOrderItemEntity(item);

                    orderItemEntity.setOrderSn(orderSn);
                    return orderItemEntity;
                })
                .collect(Collectors.toList());


        return orderItemEntityList;
    }

    private OrderItemEntity buildOrderItemEntity(OrderItemVo item) {

        OrderItemEntity orderItemEntity = new OrderItemEntity();

        // 商品的spu信息（需要远程查询）异步不需要cookie
        R r = productFeignService.getSpuInfoBySkuId(item.getSkuId());
        if (r.getCode() == 0) {
            SpuInfo spuInfo = r.getData(new TypeReference<>() {
            });
            orderItemEntity.setSpuId(spuInfo.getId());
            orderItemEntity.setSpuBrand(spuInfo.getBrandId().toString());
            orderItemEntity.setSpuName(spuInfo.getSpuName());
            orderItemEntity.setCategoryId(spuInfo.getCatalogId());
        }


        // 商品的sku信息
        orderItemEntity.setSkuId(item.getSkuId());
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());

        String skuAttr = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
        orderItemEntity.setSkuAttrsVals(skuAttr);

        // 优惠信息（不做）
        orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
        orderItemEntity.setCouponAmount(BigDecimal.ZERO);
        orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);

        // 积分信息
        orderItemEntity.setGiftGrowth(item.getPrice().intValue());
        orderItemEntity.setGiftIntegration(item.getPrice().intValue());

        // 价格信息
        BigDecimal price = orderItemEntity.getSkuPrice()
                .multiply(new BigDecimal(orderItemEntity.getSkuQuantity().toString()))
                .subtract(orderItemEntity.getPromotionAmount())
                .subtract(orderItemEntity.getCouponAmount())
                .subtract(orderItemEntity.getIntegrationAmount());
        orderItemEntity.setIntegrationAmount(price);

        return orderItemEntity;
    }

}