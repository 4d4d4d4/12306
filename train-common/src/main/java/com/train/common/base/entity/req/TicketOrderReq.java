package com.train.common.base.entity.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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
    private Long trainTicketId; // 票id

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date ; // 日期

    private String trainCode; // 当前票的火车编码
    private String start; // 当前票初始站
    private String end; // 当前票终点站
    private List<PassengerTicketReq> tickets; // 购票详情
    private String imageCode; // 验证码

    private String lineNumber; // 队列序号

}
