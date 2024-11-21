package com.train.common.base.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车站类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/1 下午4:11</li>
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
public class DailyStationVo implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String trainCode;

    private Integer index;

    private String name;

    private String namePinyin;

    @JsonFormat(pattern = "hh:mm:ss")
    @DateTimeFormat(pattern = " hh:mm:ss")
    private Date inTime; // 进站时间

    @JsonFormat(pattern = "hh:mm:ss")
    @DateTimeFormat(pattern = "hh:mm:ss")
    private Date outTime; // 出站时间

    @JsonFormat(pattern = "hh:mm:ss")
    @DateTimeFormat(pattern = "hh:mm:ss")
    private Date stopTime; // 暂停时间

    private BigDecimal km;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd hh:mm:ss")
    private Date updateTime;

}
