package com.train.business.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.DailyTrainMapper;
import com.train.common.base.entity.domain.DailyTrain;
import com.train.common.base.entity.domain.DailyTrainTicket;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.query.*;
import com.train.common.base.entity.vo.DailyTrainVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.service.BaseService;
import com.train.common.base.service.DailyTrainService;
import com.train.common.base.service.TrainService;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日火车数据接口实现</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/31 下午5:41</li>
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
public class DailyTrainServiceImpl extends BaseService implements DailyTrainService  {
    private static final Logger log = LoggerFactory.getLogger(DailyTrainServiceImpl.class);
    @Autowired
    private DailyTrainMapper dailyTrainMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @DubboReference(version = "1.0.0", check = false)
    private TrainService trainService;

    @Override
    public PaginationResultVo<DailyTrain> getAllDTrainByConditionWithPage(DailyTrainQuery trainQuery) {
        log.info("每日火车查询条件：{}", JSON.toJSONString(trainQuery));
        PaginationResultVo<DailyTrain> resultVo = new PaginationResultVo<>();
        // 分页条件
        Integer currentPage = trainQuery.getCurrentPage();
        Integer pageSize = trainQuery.getPageSize();
        // 查询条件
        String code = trainQuery.getCode(); // 模糊查询
        if(Objects.equals(code, "null")){
            code = "";
        }
        Date date = trainQuery.getDate();// 精确查询

        // 根据查询条件查询这次数据总量
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
        if(code != null){
            criteria.andCodeLike(StringTool.concat(code));
            dailyTrainExample.or().andStartLike(StringTool.concat(code));
            dailyTrainExample.or().andEndLike(StringTool.concat(code));
        }
        if(date!=null){
            criteria.andDateEqualTo(date);
        }
        // 当前页或者页大小有数据时候进行分页查询
        if(currentPage != null || pageSize != null){
            if(currentPage == null || currentPage < 1){
                currentPage = 1;
            }
            if(pageSize == null || pageSize < 5){
                pageSize = SimplePage.PAGE_20;
            }
            // 使用pageHelper进行分页查询
            PageHelper.startPage(currentPage, pageSize);
        }
        // 进行数据查询
        List<DailyTrain> dailyTrains = dailyTrainMapper.selectByExample(dailyTrainExample);
        PageInfo<DailyTrain> pageInfo = new PageInfo<>(dailyTrains);
        // 整理返回数据
        resultVo.setResult(dailyTrains);
        resultVo.setPageCount(pageInfo.getPages());
        resultVo.setPageSize(pageInfo.getPageSize());
        resultVo.setCurrentPage(pageInfo.getPageNum());
        resultVo.setDataCount((int) pageInfo.getTotal());
        return resultVo;
    }

    @Override
    public Result removeDailyTrain(List<Long> ids) {
        if(ids.isEmpty()){
            return Result.ok().message("删除成功");
        }
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.or().andIdIn(ids);
        dailyTrainMapper.deleteByExample(dailyTrainExample);
        return Result.ok().message("删除成功");
    }

    @Override
    public Result addDailyTrain(DailyTrainVo dailyTrainVo) {
        if(dailyTrainVo == null){
            return Result.error().message("添加失败,数据为空");
        }
        DailyTrain dailyTrain = new DailyTrain();
        BeanUtils.copyProperties(dailyTrainVo, dailyTrain);
        dailyTrain.setId(idStrUtils.snowFlakeLong());
        if(dailyTrain.getCreateTime() == null){
            dailyTrain.setCreateTime(new Date());
        }
        if(dailyTrain.getUpdateTime() == null){
            dailyTrain.setUpdateTime(new Date());

        }
        int i = dailyTrainMapper.insertSelective(dailyTrain);
        return i > 0 ? Result.ok() : Result.error().message("新增火车失败");
    }

    @Override
    public Result updateDTrain(DailyTrain dailyTrain) {
        if(dailyTrain == null || dailyTrain.getId() == null){
            return Result.error().message("修改失败，数据异常");
        }
        dailyTrain.setUpdateTime(new Date());
        int i = dailyTrainMapper.updateByPrimaryKeySelective(dailyTrain);
        return i > 0 ? Result.ok().message("修改成功") : Result.error().message("修改失败，请检查数据");
    }

    @Override
    public List<DailyTrain> selectDTrainByCondition(DailyTrainQuery query) {
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        if(query != null){
            DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
            if(query.getCode() != null){
                criteria.andCodeLike(query.getCode());
            }
            if(query.getStart() != null){
                criteria.andStartLike(query.getStart());
            }
            if(query.getEnd() != null){
                criteria.andEndLike(query.getEnd());
            }
            if(query.getStartPinyin() != null){
                criteria.andStartPinyinLike(query.getStartPinyin());
            }
            if(query.getEndPinyin() != null){
                criteria.andEndPinyinLike(query.getEndPinyin());
            }
            if(query.getDate() != null){
                criteria.andDateEqualTo(query.getDate());
            }
            if(StrUtil.isNotEmpty(query.getType())){
                criteria.andTypeEqualTo(query.getType());
            }
            if(query.getEndTime() != null){
                criteria.andEndTimeEqualTo(query.getEndTime());
            }
        }
        List<DailyTrain> dailyTrains = dailyTrainMapper.selectByExample(dailyTrainExample);
        return dailyTrains;
    }

    @Override
    public void delDTrainByCondition(DailyTrainQuery query) {
        if(query != null){
            DailyTrainExample dailyTrainExample = new DailyTrainExample();
            DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
            if(query.getCode() != null){
                criteria.andCodeLike(query.getCode());
            }
            if(query.getStart() != null){
                criteria.andStartLike(query.getStart());
            }
            if(query.getEnd() != null){
                criteria.andEndLike(query.getEnd());
            }
            if(query.getStartPinyin() != null){
                criteria.andStartPinyinLike(query.getStartPinyin());
            }
            if(query.getEndPinyin() != null){
                criteria.andEndPinyinLike(query.getEndPinyin());
            }
            if(query.getDate() != null){
                criteria.andDateEqualTo(query.getDate());
            }
            if(StrUtil.isNotEmpty(query.getType())){
                criteria.andTypeEqualTo(query.getType());
            }
            if(query.getEndTime() != null){
                criteria.andEndTimeEqualTo(query.getEndTime());
            }
            if(criteria.isValid()){
                int i = dailyTrainMapper.deleteByExample(dailyTrainExample);
                log.info("批量删除每日火车数据:【{}】", i);
            }

        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchInsertDTrainData(List<DailyTrain> insertList) {
        if(insertList == null ||insertList.isEmpty()){
            return;
        }
//        Map<Long, Long> collect = insertList.stream().collect(Collectors.groupingBy(DailyTrain::getId, Collectors.counting()));
//        List<Long> duplicateNames = collect.entrySet().stream()
//                .filter(entry -> entry.getValue() > 1)
//                .map(Map.Entry::getKey)
//                .toList();
//        log.info("是否有重复数据:{}", duplicateNames.size());
//        log.info("传入的数据是：{}", JSON.toJSON(insertList));
        AtomicInteger i = new AtomicInteger(1);
        insertList.forEach((item) -> {
            log.info("第{}次开始写入数据:{}",(i.getAndIncrement()) ,item);
            if(item.getId() == null){
                System.out.println("有id为空");
                item.setId(idStrUtils.snowFlakeLong());
            }
            if(item.getDate() == null){
                log.error("关键数据【日期】为空，新增数据失败");
                throw new BusinessException(ResultStatusEnum.CODE_500);
            }
            if(item.getCreateTime() == null){
                item.setCreateTime(new Date());
            }
            if(item.getUpdateTime() == null){
                item.setUpdateTime(new Date());
            }
            dailyTrainMapper.insertSelective(item);
        });

    }

    /**
     * 生成某天每日车次信息
     * @param query
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result batchInsertDTrain(DailyTrainQuery query) {
        if(query == null){
            return Result.error().message("生成每日车次失败，数据为空");
        }
        Date date = query.getDate();
        if(date == null){
            return Result.error().message("生成每日车次失败，缺少日期信息");
        }
        // 查询火车基础数据
        List<Train> trains = trainService.selectTrainsByCode("");
        if(trains.isEmpty()){
            return Result.error().message("数据库异常，缺少火车基础信息");
        }

        // 删除该date下的数据
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        dailyTrainExample.or().andDateEqualTo(date);
        dailyTrainMapper.deleteByExample(dailyTrainExample);
        Date now = new Date();

        // 添加每日数据
        trains.forEach((item) ->{
            DailyTrain dailyTrain = new DailyTrain();
            BeanUtils.copyProperties(item, dailyTrain);
            dailyTrain.setId(idStrUtils.snowFlakeLong());
            dailyTrain.setDate(date);
            dailyTrain.setCreateTime(now);
            dailyTrain.setUpdateTime(now);
            dailyTrainMapper.insertSelective(dailyTrain);
        });

        return Result.ok();
    }

    @Override
    public int delDTrainBeforeNow(Date date) {
        if(date == null || date.after(DateTime.now())){
            return 0;
        }
        DailyTrainExample dailyTrainExample = new DailyTrainExample();
        DailyTrainExample.Criteria criteria = dailyTrainExample.createCriteria();
        criteria.andDateLessThan(date);
        int count = dailyTrainMapper.deleteByExample(dailyTrainExample);
        return count;
    }

}
