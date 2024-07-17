package com.train.member.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname TestController
 * @Description 什么也没有写哦~
 * @Date 2024/7/12 下午4:11
 * @Created by 憧憬
 */
@RestController
//@CrossOrigin
public class TestController {
    @RequestMapping("/test")
    public String test(){
        return "你好，我的朋友";
    }
}
