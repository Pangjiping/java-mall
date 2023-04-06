package org.epha.mall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.epha.common.constant.CartConstant;
import org.epha.common.utils.R;
import org.epha.mall.cart.feign.ProductFeignService;
import org.epha.mall.cart.interceptor.CartInterceptor;
import org.epha.mall.cart.service.CartService;
import org.epha.mall.cart.to.SkuInfo;
import org.epha.mall.cart.vo.Cart;
import org.epha.mall.cart.vo.CartItem;
import org.epha.mall.cart.vo.UserInfo;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author pangjiping
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    ProductFeignService productFeignService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {

        // 拿到用户购物车
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();

        // 查询当前购物车中有没有这个商品，如果有的话，取出商品
        // 这个操作不需要异步
        String s = (String) cartOps.get(skuId.toString());

        // 如果购物车有这个数据，拿到数据，增加数量，再放回去
        if (StringUtils.hasText(s)) {
            CartItem item = JSON.parseObject(s, new TypeReference<CartItem>() {
            });

            item.setCount(item.getCount() + num);

            // 放回redis去
            String itemJsonString = JSON.toJSONString(item);
            cartOps.put(skuId.toString(), itemJsonString);
            return item;
        }

        CartItem cartItem = new CartItem();

        // 远程查询当前商品信息，并构建cartItem
        CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
            R r = productFeignService.getSkuInfo(skuId);
            if (r.getCode() != 0) {
                return;
            }

            SkuInfo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfo>() {
            });

            // 构建购物车item信息
            cartItem.setCheck(true);
            cartItem.setCount(num);
            cartItem.setImage(skuInfo.getSkuDefaultImg());
            cartItem.setSkuId(skuId);
            cartItem.setPrice(skuInfo.getPrice() == null ? BigDecimal.ZERO : skuInfo.getPrice());
            cartItem.setTitle(skuInfo.getSkuTitle());
        }, threadPoolExecutor);

        // 远程查询sku attrs
        CompletableFuture<Void> skuAttrFuture = CompletableFuture.runAsync(() -> {
            R r = productFeignService.getSkuSaleAttrValues(skuId);
            if (r.getCode() == 0) {
                List<String> attrs = r.getData(new TypeReference<List<String>>() {
                });
                cartItem.setSkuAttr(attrs);
            }
        }, threadPoolExecutor);

        // 等待任务完成
        CompletableFuture.allOf(skuInfoFuture, skuAttrFuture).get();

        // 把cartItem转成json string
        String cartItemJsonString = JSON.toJSONString(cartItem);

        cartOps.put(skuId.toString(), cartItemJsonString);

        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String s = (String) cartOps.get(skuId.toString());

        return JSON.parseObject(s, new TypeReference<CartItem>() {
        });
    }

    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {

        Cart cart = new Cart();

        UserInfo userInfo = CartInterceptor.threadLocal.get();
        if (userInfo.getUserId() != null) {

            // 登录了，需要合并临时购物车和登录后的购物车
            String cartKey = CartConstant.CART_CACHE_PREFIX + userInfo.getUserId().toString();

            // 拿到用户的临时购物车
            String tempCartKey = CartConstant.CART_CACHE_PREFIX + userInfo.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);

            // 如果临时购物车有数据，需要合并
            if (tempCartItems != null && tempCartItems.size() > 0) {
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
            }

            // 清空购物车
            threadPoolExecutor.execute(() ->
                    cleanupCart(tempCartKey)
            );

            // 获取登录后的购物车数据[包含合并过来的临时购物车]
            List<CartItem> items = getCartItems(cartKey);
            cart.setItems(items);

        } else {

            // 用户没登录，使用user-key
            String tempCartKey = CartConstant.CART_CACHE_PREFIX + userInfo.getUserKey();
            List<CartItem> items = getCartItems(tempCartKey);
            cart.setItems(items);
        }
        return cart;
    }

    @Override
    public void deleteCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    /**
     * 得到用户的购物车信息，绑定一个哈希表
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {

        // 拿到当前的用户信息
        UserInfo userInfo = CartInterceptor.threadLocal.get();

        String cartKey = "";

        // 如果用户登录了，就用用户Id
        // 如果用户没登录，就用临时user-key
        if (userInfo.getUserId() != null) {
            cartKey = CartConstant.CART_CACHE_PREFIX + userInfo.getUserId();
        } else {
            cartKey = CartConstant.CART_CACHE_PREFIX + userInfo.getUserKey();
        }

        // 绑定一个哈希表，之后都操作这个哈希表
        return stringRedisTemplate.boundHashOps(cartKey);
    }

    /**
     * 获取指定key的购物车
     */
    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> cartOps = stringRedisTemplate.boundHashOps(cartKey);

        List<Object> values = cartOps.values();
        if (values != null && values.size() > 0) {
            List<CartItem> cartItems = values.stream()
                    .map(o ->
                            JSON.parseObject((String) o, new TypeReference<CartItem>() {
                            })
                    )
                    .collect(Collectors.toList());

            return cartItems;
        }

        return null;
    }

    /**
     * 清空某个key的购物车
     */
    private void cleanupCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }
}
