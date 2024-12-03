package com.train.common.enums;

import lombok.Getter;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 校验码类型枚举</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/21 下午11:34</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Getter
public enum CheckCodeTypeEnum {
    LOGIN("0", "登录/注册", "0"),
    REGISTER("1", "登录/注册", "1"),
    TICKET_ORDER("2", "车票订单", "2"),
    ;
    private String type;
    private String desc;
    private String code;

    CheckCodeTypeEnum(String type, String desc, String code) {
        this.type = type;
        this.desc = desc;
        this.code = code;
    }
}
