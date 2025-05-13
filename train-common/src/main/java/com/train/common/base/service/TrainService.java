package com.train.common.base.service;

import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.query.TrainQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationVo;
import com.train.common.base.entity.vo.TrainVo;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车业务接口抽象类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/29 下午2:32</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface TrainService {
    PaginationResultVo<Train> getAllTrain(TrainQuery trainQuery);

    void addTrainList(List<TrainVo> list);

    void updateTrains(List<Train> list);

    void deleteTrainByIds(List<String> ids);

    // 根据火车编码模糊查询获取火车列表
    List<Train> selectTrainsByCode(String trainCode);

    void insert(Train train);
}
