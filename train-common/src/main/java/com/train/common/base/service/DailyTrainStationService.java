package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.query.DailyTrainStationQuery;
import com.train.common.base.entity.query.StationIndexQuery;
import com.train.common.base.entity.vo.DailyTrainStationVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationIndexVo;
import com.train.common.resp.Result;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日火车车站接口</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/2 上午2:01</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface DailyTrainStationService {
    PaginationResultVo<DailyTrainStation> selectAllDTStation(DailyTrainStationQuery query);

    Result batchDelDTStation(List<Long> ids);

    Result addDTStation(DailyTrainStationVo vo);

    Result updateDTStation(DailyTrainStation dailyTrainStation);

    List<StationIndexVo> getStationByCondition(StationIndexQuery query);

    List<DailyTrainStation> selectAllDTStationByCondition(DailyTrainStation dts);

    void batchDelTStationByCondition(DailyTrainStation trainStation);

    void batchInsertDTStation(List<DailyTrainStation> insertList);
}
