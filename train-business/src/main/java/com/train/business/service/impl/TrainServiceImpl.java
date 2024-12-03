package com.train.business.service.impl;

import com.github.pagehelper.PageHelper;
import com.train.business.mapper.StationMapper;
import com.train.business.mapper.TrainMapper;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.query.*;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.StationVo;
import com.train.common.base.entity.vo.TrainVo;
import com.train.common.base.service.TrainService;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 火车接口实现类</dd>
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
@Service
@DubboService(version = "1.0.0", token = "true")
public class TrainServiceImpl implements TrainService {

    @Autowired
    private TrainMapper trainMapper;
    @Autowired
    private StationMapper stationMapper;

    @Override
    public PaginationResultVo<Train> getAllTrain(TrainQuery trainQuery) {
        TrainExample trainExample = new TrainExample();
        trainExample.setDistinct(true);



        String code = trainQuery.getCode();
        if(code!=null) {
            trainExample.or().andCodeLike(TrainExample.concat(code));
        }
        String start = trainQuery.getStart();
        if(start != null) {
            trainExample.or().andStartLike(TrainExample.concat(start));
        }
        String startPinyin = trainQuery.getStartPinyin();
        if(startPinyin != null) {
            trainExample.or().andStartPinyinLike(TrainExample.concat(startPinyin));
        }

        String end = trainQuery.getEnd();
        if(end != null) {
            trainExample.or().andEndLike(TrainExample.concat(end));
        }

        String endPinyin = trainQuery.getEndPinyin();
        if(endPinyin != null) {
            trainExample.or().andEndPinyinLike(TrainExample.concat(endPinyin));
        }

        Integer totalCount = (int) trainMapper.countByExample(trainExample);
        Integer currentPage = trainQuery.getCurrentPage();
        Integer pageSize = trainQuery.getPageSize();

        SimplePage simplePage = new SimplePage(totalCount, currentPage, pageSize);
        if(currentPage != null || pageSize != null) {
            // 当传入的当前页和大小都为空时默认查找全部数据
            currentPage = (currentPage == null || currentPage < 1) ? 1 : currentPage;
            pageSize = pageSize == null || trainQuery.getPageSize() < 5 ? SimplePage.PAGE_20 : trainQuery.getPageSize();
            PageHelper.startPage(currentPage, pageSize);
        }
        List<Train> trains = trainMapper.selectByExample(trainExample);
        PaginationResultVo<Train> result = new PaginationResultVo<>();

        result.setPageCount(simplePage.getPageTotal());
        result.setCurrentPage(currentPage);
        result.setDataCount(totalCount);
        result.setResult(trains);
        result.setPageSize(pageSize);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addTrainList(List<TrainVo> list) {
        list.forEach((item) ->{
            Train train = new Train();
            BeanUtils.copyProperties(item, train);
            trainMapper.insertSelective(train);
        });
    }

    @Override
    public void updateTrains(List<Train> list) {
        list.forEach((item) -> {
            trainMapper.updateByPrimaryKeySelective(item);
        });

    }

    @Override
    public void deleteTrainByIds(List<String> ids) {
        ids.forEach((id) ->{
            trainMapper.deleteByPrimaryKey(Long.valueOf(id));
        });
    }

    @Override
    public List<Train> selectTrainsByCode(String trainCode) {
        TrainExample trainExample = new TrainExample();
        trainExample.or().andCodeLike(StringTool.concat(trainCode));
        return trainMapper.selectByExample(trainExample);
    }
}
