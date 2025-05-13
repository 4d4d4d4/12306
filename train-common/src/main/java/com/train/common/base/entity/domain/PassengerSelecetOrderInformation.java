package com.train.common.base.entity.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 乘客票信息查询表</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2025/3/31 下午11:48</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2025，. All rights reserved.
 * @Author cqy.
 */
@Data
public class PassengerSelecetOrderInformation implements Serializable {
    private String passengerName;
    private String passengerType;
    private String seatCode; // 座位列号 如ABCD
    private String seatType; // 座位类型 如 1-2-3-4
    private Long seatId;
    private Long ticketId;
    private Long orderId;
    private Long passengerId;
    private Integer carriageIndex;
    private String row;
    private String trainCode;
    private String start;
    private String end;
    private String startAndEndTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date;

}
