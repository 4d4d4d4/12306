package com.train.common.base.entity.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车票订单接受类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/20 下午5:20</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Data
public class TicketOrderReq implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    @NotNull(message = "【票信息】不能为空")
    private Long trainTicketId; // 票id

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "【日期】不能为空")
    private Date date ; // 日期

    @NotBlank(message = "【火车编码】不能为空")
    private String trainCode; // 当前票的火车编码

    @NotBlank(message = "【初始站】不能为空")
    private String start; // 当前票初始站

    @NotBlank(message = "【终点站】不能为空")
    private String end; // 当前票终点站

    @NotEmpty(message = "【购票详情列表】不能为空")
    private List<PassengerTicketReq> tickets; // 购票详情

    @NotBlank(message = "【验证码】不能为空")
    private String imageCode; // 验证码

    private String lineNumber; // 队列序号

}
