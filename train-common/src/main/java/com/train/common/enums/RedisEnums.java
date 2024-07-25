package com.train.common.enums;

import lombok.Getter;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : Redis常用枚举类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/23 下午5:19</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.
 * @Author 16867.
 */
@Getter
public enum RedisEnums {
    CHECK_CODE_ENUM("checkCode_", "", "验证码前缀"),
    MOBILE_SMS_ENUM("mobileSms_", "", "手机验证码");
    private String prefix; // 前缀
    private String suffix; // 后缀
    private Integer time = 60; // 保存时间 都是一分钟
    private String description; // 说明

    RedisEnums() {
    }

    RedisEnums(String prefix, String suffix, String description) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.description = description;
    }
}
