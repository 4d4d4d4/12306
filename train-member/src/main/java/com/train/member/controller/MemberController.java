package com.train.member.controller;

import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.tool.entity.CreateImageCode;
import com.train.member.entity.Member;
import com.train.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @Classname MemberController
 * @Description 什么也没有写哦~
 * @Date 2024/7/17 下午2:41
 * @Created by 憧憬
 */
@RestController
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);
    @Autowired
    private MemberService memberService;

    @RequestMapping("/count")
    public int count() {
        return memberService.count();
    }

    @RequestMapping("/register")
    public Long register(@Validated @RequestBody Member member) {
        return memberService.register(member);
    }

    /**
     * 生成验证码
     * @param response
     * @param type 验证码类型 用于 0.登录/注册
     */
    @RequestMapping("/checkCode")
    public void checkCode(HttpServletResponse response, String type) {
        CreateImageCode createImageCode = new CreateImageCode(130, 30, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        // TODO 将验证码存入缓存
        String code = createImageCode.getCode();
        try {
            createImageCode.write(response.getOutputStream());
        } catch (IOException e) {
            log.info("验证码写入失败：{}", e.getMessage());
            throw new BusinessException(ResultStatusEnum.CODE_500.getCode(), "验证码异常，请联系管理员");
        }

    }
}
