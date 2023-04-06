package org.epha.mall.member.controller;

import org.epha.common.exception.BizException;
import org.epha.common.utils.PageUtils;
import org.epha.common.utils.R;
import org.epha.mall.member.entity.MemberEntity;
import org.epha.mall.member.service.MemberService;
import org.epha.mall.member.vo.MemberLoginVo;
import org.epha.mall.member.vo.MemberRegisterVo;
import org.epha.mall.member.vo.WeiboMemberLoginVo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;


/**
 * 会员
 *
 * @author epha
 * @email 13626376642@163.com
 * @date 2023-03-23 20:33:02
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Resource
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/register")
    public R register(@RequestBody MemberRegisterVo registerVo) throws BizException {
            memberService.register(registerVo);

        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo loginVo) throws BizException {
        MemberEntity memberEntity = memberService.login(loginVo);

        // 可能需要对memberEntity做一些处理...

        return R.ok().setDate(memberEntity);
    }

    @PostMapping("/oauth/weibo/login")
    public R OAuthWeiboLogin(@RequestBody WeiboMemberLoginVo loginVo) {
        MemberEntity memberEntity = memberService.login(loginVo);

        // 可能需要对memberEntity做一些处理...

        return R.ok().setDate(memberEntity);
    }

}
