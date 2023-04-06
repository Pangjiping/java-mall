package org.epha.mall.cart.interceptor;

import org.epha.common.constant.AuthConstant;
import org.epha.common.constant.CartConstant;
import org.epha.mall.cart.vo.LoginUser;
import org.epha.mall.cart.vo.UserInfo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * 在执行目标方法之前，判断用户的登录状态，并封装传递给controller目标请求
 *
 * @author pangjiping
 */
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前
     * 检查用户的登录信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfo userInfo = new UserInfo();

        HttpSession session = request.getSession();
        LoginUser loginUser = (LoginUser) session.getAttribute(AuthConstant.SESSION_KEY_LOGIN_USER);

        // 用户登录了
        if (loginUser != null) {
            userInfo.setUserId(loginUser.getId());
        }

        // 用户没登录，拿到user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            Stream.of(cookies)
                    .forEach(cookie -> {
                        if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
                            userInfo.setUserKey(cookie.getValue());
                            userInfo.setNewTempUser(false);
                        }
                    });
        }

        // 如果没有临时用户user-key，分配一个user-key
        // 不论用户是不是登录状态
        if (!StringUtils.hasText(userInfo.getUserKey())) {
            String s = UUID.randomUUID().toString();
            userInfo.setUserKey(s);
        }

        // 把用户信息放到ThreadLocal中
        threadLocal.set(userInfo);

        return true;
    }

    /**
     * 业务执行之后
     * 给新的临时用户浏览器发送一个cookie，一个月的时间
     * TODO: 为什么配置了springSession之后，这个cookie加不上去？
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfo userInfo = threadLocal.get();

        if (userInfo.isNewTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfo.getUserKey());
            cookie.setDomain("localhost"); // 作用域
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT); // 过期时间
            response.addCookie(cookie);
        }

    }
}
