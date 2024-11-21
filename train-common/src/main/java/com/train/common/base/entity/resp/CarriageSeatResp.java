package com.train.common.base.entity.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车厢车座返回类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/17 下午5:13</li>
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
public class CarriageSeatResp implements Serializable {
    // key为座位排号(A，B,C,D)，value为座位售卖情况 value中的序号对应车座的序号
    private Map<String, List<String>> seats;

    public CarriageSeatResp() {
    }

    public CarriageSeatResp(Map<String, List<String>> seats) {
        this.seats = seats;
    }
}
