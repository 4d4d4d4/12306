package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.TrainStation;
import com.train.common.base.entity.query.DailyTrainStationQuery;
import com.train.common.base.entity.query.StationIndexQuery;
import com.train.common.base.entity.query.TrainStationQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationIndexVo;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车车站业务接口抽象类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/29 下午2:33</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface TrainStationService {
    List<TrainStation> getTrainStationByTrainCode(String trainCode);

    PaginationResultVo<TrainStation> getStationByCondition(TrainStationQuery query);

    List<TrainStation> selectAllTStationByCondition(TrainStation ts);

    Integer insertOne(TrainStation trainStation);
    Integer insertAll(List<TrainStation> list);
}
