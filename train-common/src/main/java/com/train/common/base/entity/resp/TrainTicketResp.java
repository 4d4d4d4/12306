package com.train.common.base.entity.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车票返回类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/11 上午11:26</li>
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
public class TrainTicketResp implements Serializable {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private String trainCode;

    private String start;

    private String startPinyin;

    @JsonFormat(pattern = "hh:mm:ss")
    @DateTimeFormat(pattern = "hh:mm:ss")
    private Date startTime;

    private String end;

    private String endPinyin;

    @JsonFormat(pattern = "hh:mm:ss")
    @DateTimeFormat(pattern = "hh:mm:ss")
    private Date endTime;

    private Integer ydz;

    private BigDecimal ydzPrice;

    private Integer edz;

    private BigDecimal edzPrice;

    private Integer rw;

    private BigDecimal rwPrice;

    private Integer yw;

    private BigDecimal ywPrice;

    private Boolean isOneWay = true; // 是否为单程票默认为是

    List<TrainTicketStationIndexResp> stationIndex; // 行车情况

    private TrainTicketResp goBack; // 返程票
}
