package com.train.common.base.service;

import com.train.common.base.entity.domain.Passenger;
import com.train.common.base.entity.vo.PassengerListVo;
import com.train.common.base.entity.vo.PassengerSaveVo;

import java.util.List;

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
 * <li>Date : 2024/8/5 上午6:21</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author 16867.
 */
public interface PassengerService {
    void save(PassengerSaveVo passengerSaveVo);

    List<Passenger> listByCondition(PassengerListVo passengerListVo);

    void deleteByIds(List<Long> passengerList);

    Integer listCount(PassengerListVo passengerListVo);
}
