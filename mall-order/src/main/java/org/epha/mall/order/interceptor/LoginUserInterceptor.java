package org.epha.mall.order.interceptor;

import org.epha.common.constant.AuthConstant;
import org.epha.mall.order.vo.LoginUser;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 在执行目标方法之前，判断用户的登录状态，并封装传递给controller目标请求
 *
 * @author pangjiping
 */
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();

    /**
     * 在目标方法执行之前
     * 检查用户的登录信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        LoginUser user = (LoginUser) request.getSession().getAttribute(AuthConstant.SESSION_KEY_LOGIN_USER);
        if (user != null) {
            // 拿到登录的用户
            threadLocal.set(user);
            return true;
        } else {
            // 没登录，去登录
            // 重定向到登录页（没写，用百度首页代替）
            response.sendRedirect("https://www.baidu.com");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
