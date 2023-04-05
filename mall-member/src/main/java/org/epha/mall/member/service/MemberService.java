package org.epha.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.utils.PageUtils;
import org.epha.mall.member.entity.MemberEntity;
import org.epha.mall.member.exception.AccountNotExistException;
import org.epha.mall.member.exception.PasswordMismatchException;
import org.epha.mall.member.exception.PhoneExistException;
import org.epha.mall.member.exception.UserNameExistException;
import org.epha.mall.member.vo.MemberLoginVo;
import org.epha.mall.member.vo.MemberRegisterVo;
import org.epha.mall.member.vo.WeiboMemberLoginVo;

import java.util.Map;

/**
 * 会员
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:33:02
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo registerVo) throws PhoneExistException, UserNameExistException;

    void checkUniquePhone(String email) throws PhoneExistException;

    void checkUniqueUserName(String userName) throws UserNameExistException;

    MemberEntity login(MemberLoginVo loginVo) throws AccountNotExistException, PasswordMismatchException;

    MemberEntity login(WeiboMemberLoginVo loginVo);
}

