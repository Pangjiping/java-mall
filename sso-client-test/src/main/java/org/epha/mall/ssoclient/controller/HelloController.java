package org.epha.mall.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;

/**
 * @author pangjiping
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    /**
     * 无需登录，就可以访问
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * 需要登录才可以访问
     */
    @GetMapping("/employees")
    public String employees(Model model,
                            HttpSession session,
                            @RequestParam(value = "token", required = false) String token) {

        // 只要带了token，就是从sso服务器转发过来已经登录过的
        if (StringUtils.hasText(token)) {
            // 去sso服务器获取当前token真正对应的用户信息
            ResponseEntity<String> responseEntity = new RestTemplate().getForEntity("http://localhost:8080/userInfo?token=" + token, String.class);
            session.setAttribute("loginUser", responseEntity.getBody());
        }

        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            // 没登录，重定向到sso server
            // 并且拼接上登陆成功后返回的地址
            return "redirect:" + ssoServerUrl + "?redirect_url=http://localhost:8081/employees";
        } else {
            // 登录了，展示信息
            ArrayList<String> list = new ArrayList<>();
            list.add("zhangsan");
            list.add("lisi");

            model.addAttribute("emps", list);
            return "list";
        }
    }

}
