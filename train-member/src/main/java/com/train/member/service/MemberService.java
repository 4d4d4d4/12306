package com.train.member.service;

import com.train.member.entity.dto.MemberDto;
import com.train.member.entity.vo.Member;

/**
 * @Classname MemberService
 * @Description 什么也没有写哦~
 * @Date 2024/7/17 下午2:41
 * @Created by 憧憬
 */
public interface MemberService {
    int count();

    Long register(Member member);

    MemberDto registerOrLoginMember(String mobile);
}
