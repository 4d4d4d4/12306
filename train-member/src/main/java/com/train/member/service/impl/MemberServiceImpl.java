package com.train.member.service.impl;

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
        return memberMapper.count();
    }
}
