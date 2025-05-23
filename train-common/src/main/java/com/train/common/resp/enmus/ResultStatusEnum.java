package com.train.common.resp.enmus;

import lombok.Getter;

/**
 * @Classname ResultCodeEnum
 * @Description 返回状态码枚举类
 * @Date 2024/7/17 下午4:44
 * @Created by 憧憬
 */
@Getter

public enum ResultStatusEnum {
    CODE_200(200,"请求成功"),
    CODE_500(500,"未知异常，请联系管理员"),
    CODE_501(501,"手机号不能为空"),
    CODE_502(502,"手机号异常，请稍后重新获取验证码"),
    CODE_503(503,"您10分钟内已发送3验证码，请使用最近的一次验证码或者{}分钟后重新获取"),
    CODE_504(504,"非法登录"),
    CODE_505(505,"图片校验码输入有误，请重新输入"),
    CODE_506(506,"手机验证码已过期，请重新获取"),
    CODE_507(507,"手机验证码输入有误，请重新输入或尝试再次获取"),
    CODE_508(508,"面部信息匹配失败，请重试或者尝试手机登录"),
    CODE_509(509,"该账号未开通扫脸登录，请使用手机号短信登录"),
    CODE_510(510,"余票不足"),
    CODE_511(511,"抱歉，当前购票人数过多，请稍后重试"),
    CODE_404(404,"未知路径，请求地址不存在"),
    CODE_600(600,"参数错误,可能是格式错误也可能是类型错误"),
    CODE_602(602,"参数格式错误"),
    CODE_603(603,"参数类型异常"),
    CODE_601(601,"账号信息已存在，无法注册"),
    CODE_604(604,"该手机号未注册用户。"),
    CODE_900(900,"请求超时，请重试"),
    CODE_901(901,"验证码输入错误，请重新输入"),
    ;
    private Integer code;
    private String description;

    ResultStatusEnum() {
    }

    ResultStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
