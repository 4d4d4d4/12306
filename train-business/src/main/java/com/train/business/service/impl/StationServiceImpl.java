package com.train.business.service.impl;

import com.train.business.mapper.StationMapper;
import com.train.common.base.service.StationService;
import com.train.common.base.entity.domain.Station;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.query.StationExample;
import com.train.common.base.entity.query.StationQuery;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationNameVo;
import com.train.common.base.entity.vo.StationVo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车站业务接口实现类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/29 下午2:36</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Service
@DubboService(version = "1.0.0", token = "true")
public class StationServiceImpl implements StationService {
    @Autowired
    private StationMapper stationMapper;
    @Override
    public List<StationNameVo> getAllStationName(String name) {
        StationExample stationExample = new StationExample();
        stationExample.clear();
        stationExample.setDistinct(false);
        stationExample.or().andNameLike("%" + name + "%");
        stationExample.setOrderByClause("create_time");
        List<Station> stations = stationMapper.selectByExample(stationExample);
        List<StationNameVo> stationNameVos = new ArrayList<>();
        stations.forEach((item) ->{
            StationNameVo stationNameVo = new StationNameVo();
            stationNameVo.setName(item.getName());
            stationNameVo.setName_pinyin(item.getNamePinyin());
            stationNameVo.setName_py(item.getNamePy());
            stationNameVos.add(stationNameVo);
        });
        return stationNameVos;
    }



    @Override
    @Transactional(rollbackFor = {Exception.class})
    public PaginationResultVo<Station> getAllStation(StationQuery stationQuery) {
        Integer count = stationMapper.selectAllStationCount(stationQuery);
        Integer pageSize = (stationQuery.getPageSize() != null) ? stationQuery.getPageSize() : SimplePage.PAGE_20;
        Integer currentPage = stationQuery.getCurrentPage();
        SimplePage simplePage = new SimplePage(count, currentPage,pageSize);
        List<Station> stations = stationMapper.selectByCondition(stationQuery, simplePage);
//        List<StationVo> resultList = stations.stream().map((item) -> {
//            StationVo stationVo = new StationVo();
//            BeanUtils.copyProperties(item, stationVo);
//            return stationVo;
//        }).toList();
        PaginationResultVo<Station> resultVo = new PaginationResultVo<>();
        resultVo.setDataCount(count);
        resultVo.setCurrentPage(currentPage);
        resultVo.setPageSize(simplePage.getPageSize());
        resultVo.setPageCount(simplePage.getPageTotal());
        resultVo.setResult(stations);

        return resultVo;
    }

    @Override
    public void addStationList(List<StationVo> list) {
        list.forEach((item) ->{
            // 创建和更新时间如果是null或者在未来时间则矫正
            Date now = new Date();
            if(item.getCreateTime() == null || item.getCreateTime().after(now)){
                item.setCreateTime(now);
            }
            if(item.getUpdateTime() == null || item.getUpdateTime().after(now)){
                item.setUpdateTime(now);
            }
        });
        stationMapper.addStationList(list);
    }

    @Override
    public void editStationList(List<Station> list) {
        list.forEach((item) -> {
           item.setUpdateTime(new Date());
        });
        stationMapper.editStationList(list);
    }

    @Override
    public void deleteStationByIds(List<String> ids) {
        if(!ids.isEmpty()){
            stationMapper.deleteStationByIds(ids);
        }
    }

}
