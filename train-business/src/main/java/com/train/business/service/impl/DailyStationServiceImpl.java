package com.train.business.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.DailyTrainStationMapper;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.query.DailyStationQuery;
import com.train.common.base.entity.query.DailyTrainStationExample;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.vo.DailyStationVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.service.DailyStationService;
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

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车站接口实现</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/31 下午5:43</li>
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
public class DailyStationServiceImpl implements DailyStationService {
    private static final Logger log = LoggerFactory.getLogger(DailyStationServiceImpl.class);
    @Autowired
    private DailyTrainStationMapper dStationMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @Override
    public PaginationResultVo<DailyTrainStation> getAllDStation(DailyStationQuery stationQuery) {
        if(stationQuery == null){
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        PaginationResultVo<DailyTrainStation> resultVo = new PaginationResultVo<>();
        // 分页条件
        Integer pageSize = stationQuery.getPageSize();
        Integer currentPage = stationQuery.getCurrentPage();
        if(pageSize != null || currentPage != null){
            if(pageSize == null || pageSize < 5){
                pageSize = SimplePage.PAGE_20;
            }
            if(currentPage == null || currentPage < 1){
                currentPage = 1;
            }
            PageHelper.startPage(currentPage, pageSize);
        }
        // 查询条件
        Date date = stationQuery.getDate();
        String trainCode = stationQuery.getTrainCode();
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        if(date != null){
            criteria.andDateEqualTo(date);
        }
        if(trainCode != null){
            if(Objects.equals(trainCode, "null")) {
                trainCode = "";
            }
            criteria.andTrainCodeLike(StringTool.concat(trainCode));
         }
        List<DailyTrainStation> dailyTrainStations = dStationMapper.selectByExample(dailyTrainStationExample);
        PageInfo<DailyTrainStation> page = new PageInfo<>(dailyTrainStations);

        resultVo.setResult(dailyTrainStations);
        resultVo.setDataCount((int) page.getTotal());
        resultVo.setPageSize(page.getPageSize());
        resultVo.setPageCount(page.getPages());
        resultVo.setCurrentPage(page.getPageNum());
        return resultVo;

    }

    @Override
    public Result addDStation(DailyStationVo vo) {
        if(vo == null){
            return Result.error().message("添加失败，数据为空");
        }
        DailyTrainStation dailyTrainStation = new DailyTrainStation();
        BeanUtils.copyProperties(vo, dailyTrainStation);
        if(dailyTrainStation.getCreateTime() == null){
            dailyTrainStation.setCreateTime(new Date());
        }
        if(dailyTrainStation.getUpdateTime() == null){
            dailyTrainStation.setUpdateTime(new Date());
        }
        dailyTrainStation.setId(idStrUtils.snowFlakeLong());
        int i = dStationMapper.insertSelective(dailyTrainStation);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result updateDStation(DailyTrainStation station) {
        if(station == null || station.getId() == null){
            return Result.error().message("修改失败，数据为空");
        }
        station.setUpdateTime(new Date());
        int i = dStationMapper.updateByPrimaryKeySelective(station);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result batchDelStation(List<Long> ids) {
        if(ids.isEmpty()){
            return Result.error().message("删除集合为空，请检查操作是否有误");
        }
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        dailyTrainStationExample.or().andIdIn(ids);
        int i = dStationMapper.deleteByExample(dailyTrainStationExample);
        return i > 0 ? Result.ok() : Result.error();
    }

    /**
     * 根据火车编码，日期，开始站，终点站查询包括开始站-终点站的所有途径站
     * @param trainCode 根据火车编码
     * @param date 日期
     * @param startIndex 开始站
     * @param endIndex 终点站
     * @return
     */
    @Override
    public List<DailyTrainStation> selectByIndexQuery(String trainCode, Date date, Integer startIndex, Integer endIndex) {
        DailyTrainStationExample dailyTrainStationExample = new DailyTrainStationExample();
        DailyTrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        criteria.andTrainCodeEqualTo(trainCode)
                .andDateEqualTo(date)
                .andIndexGreaterThanOrEqualTo(startIndex)
                .andIndexLessThanOrEqualTo(endIndex);
        dailyTrainStationExample.setOrderByClause("`index` asc");
        List<DailyTrainStation> dailyTrainStations = dStationMapper.selectByExample(dailyTrainStationExample);
        log.info("根据火车编码：{}，日期：{}，开始站：{}，终点站：{}查询到的结果是{}"
        ,trainCode,date,startIndex, endIndex, JSON.toJSONString(dailyTrainStations));
        return dailyTrainStations;
    }


}
