package org.epha.mall.auth.service;

import org.epha.mall.auth.vo.WeiboUserLoginVo;

public interface WeiboOAuth2Service {
    WeiboUserLoginVo getAccessToken(String code) throws Exception;
}
