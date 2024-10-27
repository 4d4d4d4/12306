package com.train.common.base.service;

import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.query.StationQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationNameVo;
import com.train.common.base.entity.vo.StationVo;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车站业务接口抽象类</dd>
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
public interface StationService {
    // 获取所有车站名称
    List<StationNameVo> getAllStationName(String name);

    // 获取所有车站数据
    PaginationResultVo<Station> getAllStation(StationQuery station);

    // 添加车站
    void addStationList(List<StationVo> list);

    // 修改车站信息
    void editStationList(List<Station> list);


    void deleteStationByIds(List<String> ids);
}
