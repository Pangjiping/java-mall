package org.epha.mall.ssoserver.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author pangjiping
 */
@Controller
public class LoginController {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/login.html")
    public String loginPage(@RequestParam("redirect_url") String url,
                            Model model,
                            @CookieValue(value = "sso_token", required = false) String token) {
        // 如果浏览器带了cookie，说明有人登录过
        // 直接重定向回去
        if (StringUtils.hasText(token)) {
            return "redirect:" + url + "?token=" + token;
        }

        // 把要返回的url地址放在页面中，传递给/doLogin
        model.addAttribute("url", url);

        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url,
                          HttpServletResponse response) {

        if (StringUtils.hasText(username) && StringUtils.hasText(password)) {

            // 保存一下用户session，先直接放在redis里面
            String token = UUID.randomUUID().toString().replace("-", "");

            stringRedisTemplate.opsForValue().set(token, username);

            // 给浏览器留一个cookie，完成同一个会话下的单点登录
            Cookie cookie = new Cookie("sso_token", token);
            response.addCookie(cookie);

            // 登录成功要跳转回之前的页面，带上token
            return "redirect:" + url + "?token=" + token;
        }

        // 登录失败，展示登录页
        return "login";
    }

    /**
     * 提供一个根据token查询用户信息的接口
     */
    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token) {
        String s = stringRedisTemplate.opsForValue().get(token);
        return s;
    }
}
