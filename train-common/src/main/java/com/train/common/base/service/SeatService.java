package com.train.common.base.service;

import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.domain.TrainSeat;
import com.train.common.base.entity.query.SeatQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.SeatColVo;
import com.train.common.base.entity.vo.SeatVo;
import com.train.common.resp.Result;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车座业务类 </dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/18 下午8:54</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface SeatService {
    Result addSeat(TrainCarriage trainCarriage, SeatVo seatVo);

    void delSeats(List<Long> ids);

    Result editSeat(TrainCarriage trainCarriage, TrainSeat trainSeat);

    PaginationResultVo<TrainSeat> selectPageByCondition(SeatQuery seatQuery);

    List<SeatColVo> getRowIndex(String trainCode, String carriageIndex, String col);
    void insertTrainSeat(TrainSeat trainSeat);

    void delSeatsByCarriage(String trainCode, Integer carriageVoIndex);
}
