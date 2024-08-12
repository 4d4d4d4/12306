package com.train.member.entity.vo;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 接收前端传入的登陆/注册信息</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/7/24 下午4:57</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024, . All rights reserved.  不能为空;\n
 * @Author 16867.
 */
@Data
public class MemberLoginVo implements Serializable {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1(3[0-9]|4[01456879]|5[0-35-9]|6[2567]|7[0-8]|8[0-9]|9[0-35-9])\\d{8}$", message = "请输入国内有效的手机号")
    private String mobile;
    @NotBlank(message = "图片校验码不能为空")
    private String checkCode;
    @NotBlank(message = "手机验证码不能为空")
    private String mobileSms;
    @Nullable
    private String type = "0"; // 类型 注册/登录类型应为0

    public MemberLoginVo() {
    }

    public MemberLoginVo(String mobile, String checkCode, String mobileSms, @Nullable String type) {
        this.mobile = mobile;
        this.checkCode = checkCode;
        this.mobileSms = mobileSms;
        this.type = type;
    }

}
