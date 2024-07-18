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
    CODE_404(404,"未知路径，请求地址不存在"),
    CODE_600(600,"参数错误,可能是格式错误也可能是类型错误"),
    CODE_602(602,"参数格式错误"),
    CODE_603(603,"参数类型异常"),
    CODE_601(601,"账号信息已存在，无法注册"),
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
