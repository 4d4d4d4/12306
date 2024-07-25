package com.train.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 存入Redisd的手机验证码类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/23 下午11:13</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, All rights reserved.
 * @Author 16867.
 */
@Data
public class RedisMobileSms implements Serializable {
    private String mobile; // 手机号
    private String code; // 验证码
    private Integer count; // 发送的次数

    @Override
    public String toString() {
        return "RedisMobileSms{" +
                "mobile='" + mobile + '\'' +
                ", code='" + code + '\'' +
                ", count=" + count +
                '}';
    }
}
