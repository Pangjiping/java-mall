package org.epha.mall.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

}