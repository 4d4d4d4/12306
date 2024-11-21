package com.train.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.TrainStationMapper;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.domain.TrainStation;
import com.train.common.base.entity.query.*;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationIndexVo;
import com.train.common.base.service.TrainStationService;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车车站接口实现类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/8/29 下午2:37</li>
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
public class TrainStationServiceImpl implements TrainStationService {
    @Autowired
    private TrainStationMapper trainStationMapper;

    /**
     * 根据火车编码精确查询经停站
     * @param trainCode
     * @return
     */
    @Override
    public List<TrainStation> getTrainStationByTrainCode(String trainCode) {
        if(StrUtil.isBlank(trainCode)){
            return List.of();
        }
        TrainStationExample trainStationExample = new TrainStationExample();
        trainStationExample.or().andTrainCodeEqualTo(trainCode);
        return trainStationMapper.selectByExample(trainStationExample);
    }

    @Override
    public PaginationResultVo<TrainStation> getStationByCondition(TrainStationQuery query) {
        if (query == null) {
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        PaginationResultVo<TrainStation> result = new PaginationResultVo<>();
        Integer currentPage = query.getCurrentPage();
        Integer pageSize = query.getPageSize();
        if (currentPage != null || pageSize != null) {
            currentPage = currentPage == null || currentPage < 1 ? currentPage = 1 : currentPage;
            pageSize = pageSize == null || pageSize < 5 ? SimplePage.PAGE_20 : pageSize;
            PageHelper.startPage(currentPage, pageSize);
        }
        String trainCode = query.getTrainCode();
        TrainStationExample dailyTrainStationExample = new TrainStationExample();
        TrainStationExample.Criteria criteria = dailyTrainStationExample.createCriteria();
        if (!StrUtil.isBlank(trainCode)) {
            criteria.andTrainCodeLike(trainCode);
        }
        List<TrainStation> dailyTrainStations = trainStationMapper.selectByExample(dailyTrainStationExample);
        PageInfo<TrainStation> page = new PageInfo<>(dailyTrainStations);
        result.setResult(dailyTrainStations);
        result.setPageCount(page.getPages());
        result.setDataCount((int) page.getTotal());
        result.setCurrentPage(page.getPageNum());
        result.setPageSize(pageSize);
        return result;
    }

    @Override
    public List<TrainStation> selectAllTStationByCondition(TrainStation dts) {
        TrainStationExample trainStationExample = new TrainStationExample();
        if(dts != null){
            TrainStationExample.Criteria criteria = trainStationExample.createCriteria();
            if(dts.getId() != null){
                criteria.andIdEqualTo(dts.getId());
            }
            if(dts.getIndex() != null){
                criteria.andIndexEqualTo(dts.getIndex());
            }
            if(dts.getName() != null){
                criteria.andNameLike(dts.getName());
            }
            if(dts.getTrainCode() != null){
                criteria.andTrainCodeEqualTo(dts.getTrainCode());
            }
            if(dts.getNamePinyin() != null){
                criteria.andNamePinyinLike(dts.getNamePinyin());
            }
        }
        return trainStationMapper.selectByExample(trainStationExample);
    }


}
