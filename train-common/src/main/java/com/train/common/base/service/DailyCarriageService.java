package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrainCarriage;
import com.train.common.base.entity.query.DailyCarriageQuery;
import com.train.common.base.entity.query.DailyTrainCarriageExample;
import com.train.common.base.entity.vo.DailyCarriageVo;
import com.train.common.base.entity.vo.DailyTrainVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.resp.Result;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车厢数据服务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/31 下午5:40</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface DailyCarriageService {
    PaginationResultVo<DailyTrainCarriage> selectAllByConditionWithPage(DailyCarriageQuery query);

    Result batchDelCarriage(List<Long> ids);

    Result addDCarriage(DailyCarriageVo carriageVo);

    Result updateDCarriage(DailyTrainCarriage carriage);

    void createCarriage(DailyCarriageVo carriageVo);

    void batchInsertCarriage(List<DailyTrainCarriage> dailyTrainCarriages);

    void batchDelCarriageByCondition(DailyCarriageQuery query);

    Result creatDayCarriage(DailyCarriageVo vo);

    List<DailyTrainCarriage> selectAllCarriageByCondition(DailyTrainCarriage dailyTrainCarriage);

    int delDCarriageBeforeNow(Date date);
}
