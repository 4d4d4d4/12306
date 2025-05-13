package com.train.common.base.entity.req;

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
 * <li>Date : 2025/4/1 上午3:06</li>
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
public class RefundTicketReq implements Serializable
{
    private Long passengerId;
    private Long ticketId;
    private Long orderId;
    private Long seatId;
}
