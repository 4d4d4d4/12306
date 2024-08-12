package com.train.common.base.service;


import com.train.common.base.entity.domain.Member;
import com.train.common.base.entity.dto.MemberDto;
import com.train.common.base.entity.query.MemberExample;

import java.util.List;

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
    List<Member>  selectMemberList(MemberExample memberExample);
}
