package com.train.member.service.impl;

import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.member.entity.Member;
import com.train.member.entity.MemberExample;
import com.train.member.mapper.MemberMapper;
import com.train.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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


    @Override
    public int count() {
        return Math.toIntExact(memberMapper.countByExample(null));
    }

    @Override
    public Long register(Member member) {
        MemberExample memberExample = new MemberExample();
        MemberExample.Criteria criteria = memberExample.createCriteria().andMobileEqualTo(member.getMobile());
        long mobileCount = memberMapper.countByExample(memberExample);
        if(mobileCount > 0){
            throw new BusinessException(ResultStatusEnum.CODE_601);
        }
        Member registerMember = new Member();
        long id = System.currentTimeMillis();
        registerMember.setId(id);
        registerMember.setMobile(member.getMobile());
        memberMapper.insertSelective(registerMember);
        return id;
    }
}
