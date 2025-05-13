package com.train.common.base.entity.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : </dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2025/3/31 下午7:10</li>
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
public class PassengerTicketsVo implements Serializable {

    private Long passengerId;
    public PassengerTicketsVo() {}

    public PassengerTicketsVo(Long passengerId) {
        this.passengerId = passengerId;
    }
}
