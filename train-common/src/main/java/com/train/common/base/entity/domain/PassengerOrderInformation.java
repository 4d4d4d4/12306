package com.train.common.base.entity.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 乘车人订单表</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/22 下午2:29</li>
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
public class PassengerOrderInformation {
    private Long passengerId;
    private String passengerName;
    private String passengerType;
    private String seatCode; // 座位列号 如ABCD
    private String seatType; // 座位类型 如 1-2-3-4

    @JsonIgnore
    private Integer index; // 偏移值

    // 座位id 车厢序号 row 车座相对车厢的位置序号
    private Long seatId;
    private Integer carriageIndex;
    private String row;
    private Integer carriageSeatIndex;

}
