package org.epha.mall.cart.controller;

import org.epha.common.utils.R;
import org.epha.mall.cart.service.CartService;
import org.epha.mall.cart.vo.Cart;
import org.epha.mall.cart.vo.CartItem;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author pangjiping
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    CartService cartService;

    /**
     * 查询购物车所有选中的购物项
     */
    @GetMapping("/check/items")
    public R getCheckedItems(){
        List<CartItem> cartItems = cartService.getCheckedItems();

        return R.ok().setDate(cartItems);
    }

    /**
     * 展示购物车首页，需要用户鉴权
     */
    @GetMapping("/cart.html")
    public R cartListPage() throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();

        return R.ok().setDate(cart);
    }

    /**
     * 将商品添加到购物车
     */
    @GetMapping("/add")
    public R addToCart(@RequestParam("skuId") Long skuId,
                       @RequestParam("num") Integer num) throws ExecutionException, InterruptedException {

        CartItem cartItem = cartService.addToCart(skuId, num);

        return R.ok().setDate(cartItem);
    }

    /**
     * 得到购物车中某件商品
     */
    @GetMapping("/list/{skuId}")
    public R listSku(@PathVariable("skuId") Long skuId) {

        CartItem cartItem = cartService.getCartItem(skuId);

        return R.ok().setDate(cartItem);
    }

    @DeleteMapping("/{skuId}")
    public R deleteCartItem(@PathVariable("skuId") Long skuId) {
        cartService.deleteCartItem(skuId);

        return R.ok();
    }

}
