package org.epha.mall.cart.service;

import org.epha.mall.cart.vo.Cart;
import org.epha.mall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void deleteCartItem(Long skuId);
}
