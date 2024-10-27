package com.train.common.base.service;

import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.query.CarriageQuery;
import com.train.common.base.entity.vo.CarriageIndexVo;
import com.train.common.base.entity.vo.CarriageVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.TrainCodeVo;
import com.train.common.resp.Result;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车厢管理服务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/17 下午3:55</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public interface CarriageService {

    // 批量添加车厢
    String batchAdd(List<CarriageVo> carriageVoList);

    // 批量删除
    void batchDelete(List<Long> ids);

    // 根据id 或者 火车编码以及index进行修改
    Result updateCarriage(TrainCarriage trainCarriage);

    PaginationResultVo<TrainCarriage> selectAllByConditionWithPage(CarriageQuery query);

    // 根据火车code获取车厢情况
    List<CarriageIndexVo> getAllCarriagesIndex(String trainCode);

    // 根据火车编码模糊查询火车
    List<TrainCodeVo> getAllTrainByCode(String trainCode);

    // 根据车厢id获取车厢数据
    TrainCarriage selectByTrainCodeAndCarriageIndex(String trainCode, Integer carriageIndex);

    // 根据车厢生成车座
    void createCarriage(CarriageVo carriageVo);
}
