package com.train.common.base.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;


/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 乘车人新增类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/5 上午6:15</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author 16867.
 */
@Data
public class PassengerSaveVo implements Serializable {
    @NotBlank(message = "乘车人名称不能为空")
    private String name;
    @NotBlank(message = "身份证号码不能为空")
    @Pattern(regexp = "^(1[0-9]{16}|2[0-4][0-9]{15}|3[0-9]{15}|[456789][0-9]{14})([0-9]|X)$", message = "请输入正确的18为身份证号")
    private String idCard;
    @NotBlank(message = "请选择乘车人类型")
    private String type;


}
