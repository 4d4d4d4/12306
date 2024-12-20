package com.train.member.service.impl;

import cn.hutool.core.util.IdUtil;
import com.train.common.base.entity.domain.Member;
import com.train.common.base.entity.dto.MemberDto;
import com.train.common.base.entity.query.MemberExample;
import com.train.common.base.service.MemberService;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.JwtUtil;
import com.train.member.mapper.MemberMapper;
import io.jsonwebtoken.Claims;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Classname MemberServiceImpl
 * @Description MemberService实现类
 * @Date 2024/7/17 下午2:42
 * @Created by 憧憬
 */
@Service
@DubboService(version = "1.0.0", token = "true")
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @Autowired
    private JwtUtil jwtUtil;


    @Override
    public int count() {
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    @Override
    public Long register(Member member) {
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(member.getMobile());
        long mobileCount = memberMapper.countByExample(memberExample);
        if(mobileCount > 0){
            throw new BusinessException(ResultStatusEnum.CODE_601);
        }
        Member registerMember = new Member();
        long id = IdUtil.getSnowflake(1, 1).nextId();
        registerMember.setId(id);
        registerMember.setMobile(member.getMobile());
        memberMapper.insertSelective(registerMember);
        return id;
    }

    @Override
    public MemberDto registerOrLoginMember(String mobile) {
        MemberDto memberDto = new MemberDto();
        MemberExample memberExample = new MemberExample();
        memberExample.createCriteria().andMobileEqualTo(mobile);
        // 根据手机号查询用户列表
        List<Member> members = memberMapper.selectByExample(memberExample);
        if(!members.isEmpty()){
            Member member = members.get(0);
            Map<String, Object> params = new HashMap<>();
            params.put("memberId", member.getId());
            String jwt = jwtUtil.createJWT(params);
            // 测试
            jwtUtil.parseJWT(jwt);
            memberDto.setToken(jwt);

            // 已注册
            return memberDto;
        }
        Long id = idStrUtils.snowFlakeLong();
        Member member = new Member();
        member.setId(id);
        member.setMobile(mobile);
        memberMapper.insertSelective(member);

        Map<String, Object> params = new HashMap<>();
        params.put("memberId", id);
        String jwt = jwtUtil.createJWT(params);
        System.out.println("创建的jwt:" + jwt);
        memberDto.setToken(jwt);

        return memberDto;
    }

    @Override
    public List<Member> selectMemberList(MemberExample memberExample) {
        return memberMapper.selectByExample(memberExample);
    }
}
