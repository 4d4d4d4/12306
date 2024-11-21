package com.train.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.DailyTrainStationMapper;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.query.DailyTrainStationExample;
import com.train.common.base.entity.query.DailyTrainStationQuery;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.query.StationIndexQuery;
import com.train.common.base.entity.vo.DailyTrainStationVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationIndexVo;
import com.train.common.base.service.DailyTrainStationService;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日火车车站接口实现</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/2 上午2:02</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@DubboService(version = "1.0.0", token = "true")
public class DailyTrainStationServiceImple implements DailyTrainStationService {

    private static final Logger log = LoggerFactory.getLogger(DailyTrainStationServiceImple.class);
    @Autowired
    private DailyTrainStationMapper dailyTrainStationMapper;
    @Autowired
    private IdStrUtils idStrUtils;

    @Override
    public PaginationResultVo<DailyTrainStation> selectAllDTStation(DailyTrainStationQuery query) {
        if (query == null) {
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        PaginationResultVo<DailyTrainStation> result = new PaginationResultVo<>();
        Integer currentPage = query.getCurrentPage();
        Integer pageSize = query.getPageSize();
        if (currentPage != null || pageSize != null) {
            currentPage = currentPage == null || currentPage < 1 ? currentPage = 1 : currentPage;
            pageSize = pageSize == null || pageSize < 5 ? SimplePage.PAGE_20 : pageSize;
            PageHelper.startPage(currentPage, pageSize);
        }
        Date date = query.getDate();
        String trainCode = query.getTrainCode();
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        if (date != null) {
            criteria.andDateEqualTo(date);
        }
        if (!StrUtil.isBlank(trainCode)) {
            criteria.andTrainCodeEqualTo(trainCode);
        }
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        PageInfo<DailyTrainStation> page = new PageInfo<>(dailyTrainStations);
        result.setResult(dailyTrainStations);
        result.setPageCount(page.getPages());
        result.setDataCount((int) page.getTotal());
        result.setCurrentPage(page.getPageNum());
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public Result batchDelDTStation(List<Long> ids) {
        if (ids.isEmpty()) {
            return Result.error().message("数据为空，删除失败");
        }
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        criteria.andIdIn(ids);
        int i = dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result addDTStation(DailyTrainStationVo vo) {
        if (vo == null) {
            return Result.error().message("添加失败,数据为空");
        }
        DailyTrainStation dailyTrainStation = new DailyTrainStation();
        BeanUtils.copyProperties(vo, dailyTrainStation);
        dailyTrainStation.setId(idStrUtils.snowFlakeLong());
        if (dailyTrainStation.getCreateTime() == null) {
            dailyTrainStation.setCreateTime(new Date());
        }
        if (dailyTrainStation.getUpdateTime() == null) {
            dailyTrainStation.setUpdateTime(new Date());
        }
        int i = dailyTrainStationMapper.insertSelective(dailyTrainStation);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result updateDTStation(DailyTrainStation dailyTrainStation) {
        if (dailyTrainStation == null || dailyTrainStation.getId() == null) {
            return Result.error().message("数据为空添加失败");
        }
        dailyTrainStation.setUpdateTime(new Date());
        int i = dailyTrainStationMapper.updateByPrimaryKeySelective(dailyTrainStation);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public List<StationIndexVo> getStationByCondition(StationIndexQuery query) {
        if (query == null) {
            return List.of();
        }
        List<StationIndexVo> result = new ArrayList<>();
        Date date = query.getDate();
        String trainCode = query.getTrainCode();
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        if (date != null) {
            criteria.andDateEqualTo(date);
        }
        if (trainCode != null) {
            criteria.andTrainCodeEqualTo(trainCode);
        }
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
        if (!dailyTrainStations.isEmpty()) {
            dailyTrainStations.forEach((item) -> {
                StationIndexVo stationIndexVo = new StationIndexVo();
                stationIndexVo.setDate(item.getDate());
                stationIndexVo.setIndex(item.getIndex());
                stationIndexVo.setName(item.getName());
                stationIndexVo.setOutTime(item.getOutTime());
                stationIndexVo.setInTime(item.getInTime());
                stationIndexVo.setStopTime(item.getStopTime());
                result.add(stationIndexVo);
            });

        }

        return result;
    }

    @Override
    public List<DailyTrainStation> selectAllDTStationByCondition(DailyTrainStation dts) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        if (dts != null) {
            DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
            if (dts.getId() != null) {
                criteria.andIdEqualTo(dts.getId());
            }
            if (dts.getIndex() != null) {
                criteria.andIndexEqualTo(dts.getIndex());
            }
            if (dts.getDate() != null) {
                criteria.andDateEqualTo(dts.getDate());
            }
            if (StrUtil.isNotBlank(dts.getName())) {
                criteria.andNameLike(StringTool.concat(dts.getName()));
            }
            if (StrUtil.isNotBlank(dts.getTrainCode() )) {
                criteria.andTrainCodeEqualTo(dts.getTrainCode());
            }
            if (StrUtil.isNotBlank(dts.getNamePinyin())) {
                criteria.andNamePinyinLike(StringTool.concat(dts.getNamePinyin()));
            }
        }
        return dailyTrainStationMapper.selectByExample(dailyTrainStationExample);
    }

    @Override
    public void batchDelTStationByCondition(DailyTrainStation dts) {
        if (dts == null) {
            return;
        }
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        if (dts.getId() != null) {
            criteria.andIdEqualTo(dts.getId());
        }
        if (dts.getIndex() != null) {
            criteria.andIndexEqualTo(dts.getIndex());
        }
        if (dts.getDate() != null) {
            criteria.andDateEqualTo(dts.getDate());
        }
        if (StrUtil.isNotBlank(dts.getName())) {
            criteria.andNameLike(StringTool.concat(dts.getName()));
        }
        if (StrUtil.isNotBlank(dts.getTrainCode() )) {
            criteria.andTrainCodeEqualTo(dts.getTrainCode());
        }
        if (StrUtil.isNotBlank(dts.getNamePinyin())) {
            criteria.andNamePinyinLike(StringTool.concat(dts.getNamePinyin()));
        }
        if(criteria.isValid()) {
            int i = dailyTrainStationMapper.deleteByExample(dailyTrainStationExample);
            log.info("批量删除了火车车站数据有：【{}】", i );
        }
    }

    @Override
    public void batchInsertDTStation(List<DailyTrainStation> insertList) {
        if(insertList.isEmpty()){
            return;
        }
        log.info("开始批量插入火车车站数据:【{}】", JSON.toJSONString(insertList));
        insertList.forEach((item) -> {
            if(item.getId() == null){
                item.setId(idStrUtils.snowFlakeLong());
            }
            if(item.getCreateTime() != null){
                item.setCreateTime(new Date());
            }
            if(item.getUpdateTime() != null){
                item.setUpdateTime(new Date());
            }
            dailyTrainStationMapper.insertSelective(item);
        });
    }
}
