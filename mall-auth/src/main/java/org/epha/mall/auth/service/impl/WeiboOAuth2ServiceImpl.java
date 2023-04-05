package org.epha.mall.auth.service.impl;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.epha.common.utils.HttpUtils;
import org.epha.mall.auth.service.WeiboOAuth2Service;
import org.epha.mall.auth.vo.WeiboUserLoginVo;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @author pangjiping
 */
@Service
public class WeiboOAuth2ServiceImpl implements WeiboOAuth2Service {
    @Override
    public WeiboUserLoginVo getAccessToken(String code) throws Exception {
        // 根据code换取access-token
        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "2636917288");
        map.put("client_secret", "fghudkshfiusdhfiudsf");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://localhost:8888/auth/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost(
                "api.weibo.com",
                "/oauth2/access_token",
                "post",
                null,
                null,
                map
        );

        // 处理
        if (response.getStatusLine().getStatusCode() != 200) {
            return null;
        }

        String responseBody = EntityUtils.toString(response.getEntity());
        return JSON.parseObject(responseBody, WeiboUserLoginVo.class);
    }
}
