package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrain;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.query.DailyTrainQuery;
import com.train.common.base.entity.vo.DailyTrainVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.resp.Result;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日火车数据接口</dd>
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

public interface DailyTrainService {
    PaginationResultVo<DailyTrain> getAllDTrainByConditionWithPage(DailyTrainQuery trainQuery);

    Result removeDailyTrain(List<Long> ids);

    Result addDailyTrain(DailyTrainVo dailyTrainVo);

    Result updateDTrain(DailyTrain dailyTrain);

    List<DailyTrain> selectDTrainByCondition(DailyTrainQuery query);

    void delDTrainByCondition(DailyTrainQuery dailyTrainQuery);

    void batchInsertDTrainData(List<DailyTrain> insertList);

    Result batchInsertDTrain(DailyTrainQuery query);

    int delDTrainBeforeNow(Date date);
}
