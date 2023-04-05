package org.epha.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.epha.common.utils.HttpUtils;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.Query;
import org.epha.mall.member.dao.MemberDao;
import org.epha.mall.member.entity.MemberEntity;
import org.epha.mall.member.exception.AccountNotExistException;
import org.epha.mall.member.exception.PasswordMismatchException;
import org.epha.mall.member.exception.PhoneExistException;
import org.epha.mall.member.exception.UserNameExistException;
import org.epha.mall.member.service.MemberLevelService;
import org.epha.mall.member.service.MemberService;
import org.epha.mall.member.vo.MemberLoginVo;
import org.epha.mall.member.vo.MemberRegisterVo;
import org.epha.mall.member.vo.WeiboMemberLoginVo;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;


/**
 * @author pangjiping
 */
@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Resource
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo registerVo) throws PhoneExistException, UserNameExistException {
        MemberEntity memberEntity = new MemberEntity();

        // 设置默认等级
//        MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultLevel();
//        memberEntity.setLevelId(memberLevelEntity.getId());

        // 检查用户名和手机号的唯一，使用异常机制，直接抛出去
        checkUniquePhone(registerVo.getPhone());
        checkUniqueUserName(registerVo.getUserName());

        memberEntity.setMobile(registerVo.getPhone());
        memberEntity.setUsername(registerVo.getUserName());

        // 设置密码，加密存储密码
        String encodedPassword = new BCryptPasswordEncoder().encode(registerVo.getPassword());
        memberEntity.setPassword(encodedPassword);

        // 其他的默认信息...

        getBaseMapper().insert(memberEntity);
    }

    @Override
    public void checkUniquePhone(String phone) throws PhoneExistException {

        Integer count = getBaseMapper().selectCount(
                new QueryWrapper<MemberEntity>().eq("mobile", phone)
        );

        // 如果存在了手机号，抛出异常
        if (count > 0) {
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUniqueUserName(String userName) throws UserNameExistException {
        Integer count = getBaseMapper().selectCount(
                new QueryWrapper<MemberEntity>().eq("username", userName)
        );

        // 如果存在了用户名，抛出异常
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo loginVo) throws AccountNotExistException, PasswordMismatchException {

        MemberEntity memberEntity = getBaseMapper().selectOne(
                new QueryWrapper<MemberEntity>()
                        .eq("username", loginVo.getLoginAccount())
                        .or()
                        .eq("mobile", loginVo.getLoginAccount())
        );

        if (memberEntity == null) {
            throw new AccountNotExistException();
        }

        // 得到密码，匹配用户输入的密码
        boolean matches = new BCryptPasswordEncoder().matches(loginVo.getPassword(), memberEntity.getPassword());
        if (!matches) {
            throw new PasswordMismatchException();
        }

        return memberEntity;
    }

    /**
     * 用nickname代替uid，不改变表结构了
     * 用email代替access_token
     * 用level_id代替expires_in
     */
    @Override
    public MemberEntity login(WeiboMemberLoginVo loginVo) {

        String uid = loginVo.getUid();

        // 判断当前社交用户是否登录过系统
        MemberEntity memberEntity = getBaseMapper().selectOne(
                new QueryWrapper<MemberEntity>().eq("nickname", uid)
        );

        // 这个用户已经注册过了
        if (memberEntity != null) {

            // 更新令牌和过期时间
            MemberEntity updateEntity = new MemberEntity();
            updateEntity.setId(memberEntity.getId());
            updateEntity.setEmail(loginVo.getAccess_token());
            updateEntity.setLevelId(loginVo.getExpires_in());

            getBaseMapper().updateById(updateEntity);

            memberEntity.setEmail(loginVo.getAccess_token());
            memberEntity.setLevelId(loginVo.getExpires_in());
            return memberEntity;
        }

        // 用户还没有注册过
        MemberEntity newEntity = new MemberEntity();
        newEntity.setNickname(uid);
        newEntity.setEmail(loginVo.getAccess_token());
        newEntity.setLevelId(loginVo.getExpires_in());

        // 查到当前社交用户的社交账号信息（手机号、username）
        try {
            WeiboUserInfo weiboUserInfo = fetchWeiboUserInfo(loginVo);

            // 同步用户信息
            if (weiboUserInfo != null) {
                newEntity.setUsername(weiboUserInfo.getUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 插入数据
        getBaseMapper().insert(newEntity);

        return newEntity;
    }

    private WeiboUserInfo fetchWeiboUserInfo(WeiboMemberLoginVo loginVo) throws Exception {

        HashMap<String, String> query = new HashMap<>();
        query.put("access_token", loginVo.getAccess_token());
        query.put("uid", loginVo.getUid());

        HttpResponse response = HttpUtils.doGet(
                "https://api.weibo.com",
                "/2/users/show.json",
                "get",
                new HashMap<String, String>(),
                query
        );

        if (response.getStatusLine().getStatusCode() == 200) {
            String jsonString = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = JSON.parseObject(jsonString);

            // 获取部分用户信息
            WeiboUserInfo userInfo = new WeiboUserInfo();
            userInfo.setUsername(jsonObject.getString("name"));
            // ....

            return userInfo;
        }

        return null;
    }

    @Data
    private static class WeiboUserInfo {
        private String username;
    }

}