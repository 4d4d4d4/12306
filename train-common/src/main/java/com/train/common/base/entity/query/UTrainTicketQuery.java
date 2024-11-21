package com.train.common.base.entity.query;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 用户端火车票查询</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/10 下午4:31</li>
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
public class UTrainTicketQuery implements Serializable {
    private String departure; // 起始站 拼音全称

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date departureDate; // 起始时间

    private String destination; // 终点站 拼音全称

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date destinationDate; // 终止时间

    private boolean highSpeed; // 是否高铁 如果是ture 则只查询G开头的火车

    private  boolean student; // 是否学生票 统一降价

    private Integer tickFormModel; // 选择模式 0->单程 1->往返
 }
