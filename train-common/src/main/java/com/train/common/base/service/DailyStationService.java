package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.query.DailyStationQuery;
import com.train.common.base.entity.vo.DailyStationVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.resp.Result;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车站服务抽象</dd>
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
public interface DailyStationService {

    PaginationResultVo<DailyTrainStation> getAllDStation(DailyStationQuery stationQuery);

    Result addDStation(DailyStationVo vo);

    Result updateDStation(DailyTrainStation station);

    Result batchDelStation(List<Long> ids);

    List<DailyTrainStation> selectByIndexQuery(String trainCode, Date currentDate , Integer startIndex, Integer endIndex);
}
