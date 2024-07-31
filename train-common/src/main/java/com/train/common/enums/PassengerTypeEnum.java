package com.train.common.enums;

import lombok.Getter;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 乘客枚举类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/30 下午2:49</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, All rights reserved.
 * @Author 16867.
 */
@Getter
public enum PassengerTypeEnum {
    ADULT(1,"成人"),
    CHILD(2,"儿童"),
    STUDENT(3,"学生");
    private Integer type; // 乘客类型值
    private String desc; // 乘客类型描述

    PassengerTypeEnum(Integer type, String desc){
        this.type = type;
        this.desc = desc;
    }
}
