package com.train.member.controller;

import com.train.member.entity.Member;
import com.train.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname MemberController
 * @Description 什么也没有写哦~
 * @Date 2024/7/17 下午2:41
 * @Created by 憧憬
 */
@RestController
@RequestMapping("/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @RequestMapping("/count")
    public int count(){
        return memberService.count();
    }

    @RequestMapping("/register")
    public Long register(@Validated @RequestBody Member member){
        return memberService.register(member);
    }
}
