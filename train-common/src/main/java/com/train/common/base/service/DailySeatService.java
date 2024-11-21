package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrainSeat;
import com.train.common.base.entity.query.DailySeatQuery;
import com.train.common.base.entity.vo.DailySeatVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.resp.Result;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车座抽象</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/31 下午5:41</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface DailySeatService {
    PaginationResultVo<DailyTrainSeat> queryAllDSeat(DailySeatQuery q);

    Result batchDelSeat(List<Long> ids);

    Result insertDSeat(DailySeatVo seatVo);

    Result updateDSeat(DailyTrainSeat seat);

    void insertSelective(DailyTrainSeat trainSeat);

    void batchDelSeatByCondition(DailyTrainSeat dailyTrainSeat);

    Result createDSeat(DailySeatVo vo);

    void batchInsertDSeat(List<DailyTrainSeat> insertSeats);

    List<DailyTrainSeat> selectDSeatByCondition(DailyTrainSeat o);

    List<DailyTrainSeat> selectDSeatByConditionExample(DailyTrainSeat dailyTrainSeat);
}
