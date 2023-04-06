package org.epha.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.epha.common.exception.BizException;
import org.epha.common.utils.PageUtils;
import org.epha.mall.member.entity.MemberEntity;
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

    void register(MemberRegisterVo registerVo) throws BizException;

    void checkUniquePhone(String email) throws  BizException;

    void checkUniqueUserName(String userName) throws  BizException;

    MemberEntity login(MemberLoginVo loginVo) throws  BizException;

    MemberEntity login(WeiboMemberLoginVo loginVo);
}

