package com.train.member.service.impl;

import cn.hutool.core.util.IdUtil;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.JwtUtil;
import com.train.member.entity.dto.MemberDto;
import com.train.member.entity.vo.Member;
import com.train.member.entity.query.MemberExample;
import com.train.member.mapper.MemberMapper;
import com.train.member.service.MemberService;
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
public class MemberServiceImpl implements MemberService {

    @Autowired
    private MemberMapper memberMapper;
    @Autowired
    private IdStrUtils idStrUtils;


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
            String jwt = JwtUtil.createJWT(params);
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
        String jwt = JwtUtil.createJWT(params);
        memberDto.setToken(jwt);

        return memberDto;
    }
}
