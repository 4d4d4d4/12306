package com.train.business.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.DailyTrainCarriageMapper;
import com.train.common.base.entity.domain.*;
import com.train.common.base.entity.query.*;
import com.train.common.base.entity.vo.DailyCarriageVo;
import com.train.common.base.entity.vo.DailyTrainVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.service.*;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车厢接口实现</dd>
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
public class DailyCarriageServiceImpl extends BaseService implements DailyCarriageService {
    private static final Logger logger = LoggerFactory.getLogger(DailyCarriageServiceImpl.class);
    @Autowired
    private DailyTrainCarriageMapper dailyCarriageMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @DubboReference(version = "1.0.0", check = false)
    private TrainService trainService;
    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dailySeatService;
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false)
    private CarriageService carriageService;

    @Override
    public PaginationResultVo<DailyTrainCarriage> selectAllByConditionWithPage(DailyCarriageQuery query) {
        if (query == null) {
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        PaginationResultVo<DailyTrainCarriage> resultVo = new PaginationResultVo<>();
        // 分页查询条件
        Integer pageSize = query.getPageSize();
        Integer currentPage = query.getCurrentPage();
        // 根据分页查询条件判断是否需要分页查询
        if (pageSize != null || currentPage != null) {
            pageSize = pageSize == null || pageSize < 5 ? SimplePage.PAGE_20 : pageSize;
            currentPage = currentPage == null || currentPage < 0 ? 1 : currentPage;
            PageHelper.startPage(currentPage, pageSize);
        }
        // 条件查询条件
        Date date = query.getDate();
        String trainCode = query.getTrainCode();
        if(trainCode != null && trainCode.equals("null")){
            trainCode = "";
        }
        // 构建条件查询
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        if(trainCode != null){
            criteria.andTrainCodeLike(StringTool.concat(trainCode));
        }
        if(date != null){
            criteria.andDateEqualTo(date);
        }
        // 进行条件查询
        List<DailyTrainCarriage> dailyTrainCarriages = dailyCarriageMapper.selectByExample(dailyTrainCarriageExample);
        // 计算分页查询数据
        PageInfo<DailyTrainCarriage> pageInfo = new PageInfo<>(dailyTrainCarriages);
        // 构建返回对象
        resultVo.setResult(dailyTrainCarriages);
        resultVo.setPageSize(pageInfo.getPageSize());
        resultVo.setCurrentPage(pageInfo.getPageNum());
        resultVo.setPageCount(pageInfo.getPages());
        resultVo.setDataCount((int) pageInfo.getTotal());
        return resultVo;
    }

    @Override
    public Result batchDelCarriage(List<Long> ids) {
        if(ids.isEmpty()){
            return Result.error().message("数据为空，删除失败");
        }
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.or().andIdIn(ids);
        int i = dailyCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result addDCarriage(DailyCarriageVo carriageVo) {
        if(carriageVo == null){
            return Result.error().message("数据异常");
        }
        DailyTrainCarriage trainCarriage = new DailyTrainCarriage();
        BeanUtils.copyProperties(carriageVo, trainCarriage);
        trainCarriage.setId(idStrUtils.snowFlakeLong());
        if(trainCarriage.getCreateTime()== null){
            trainCarriage.setCreateTime(new Date());
        }
        if(trainCarriage.getUpdateTime() == null){
            trainCarriage.setUpdateTime(new Date());
        }
        int i = dailyCarriageMapper.insertSelective(trainCarriage);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result updateDCarriage(DailyTrainCarriage carriage) {
        if(carriage == null){
            return Result.error().message("修改失败,数据为空");
        }
        carriage.setUpdateTime(new Date());
        int i = dailyCarriageMapper.updateByPrimaryKeySelective(carriage);
        return i > 0 ? Result.ok() : Result.error().message("未找到数据");
    }

    @Override
    public void createCarriage(DailyCarriageVo carriageVo) {
        Date date = carriageVo.getDate();
        String trainCode = carriageVo.getTrainCode();
        String seatType = carriageVo.getSeatType();
        Integer carriageVoIndex = carriageVo.getIndex();
        List<Train> trains = trainService.selectTrainsByCode(trainCode);
        if(trains.isEmpty()){
            throw new BusinessException(500, "系统错误，请核实火车编码是否正确");
        }
        // 删除每日车座数据中该车厢的数据
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        dailyTrainCarriageExample.or().andTrainCodeEqualTo(trainCode).andIndexEqualTo(carriageVoIndex);
        dailyCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        // 遍历车厢中所有车座新增车座
        Integer rowCount = carriageVo.getRowCount();
        Integer colCount = carriageVo.getColCount();
        List<TrainStation> trainStations = trainStationService.getTrainStationByTrainCode(trainCode);
        StringBuilder sell = new StringBuilder();
        // 若经停4站则为000 每个零分别代表 1-2 2-3 3-4 站该车位是否可以购买 0为可以购买 1为不可以
        for(int i = 1; i < trainStations.size(); i++){
            sell.append("0");
        }
        for(int row = 1; row <= rowCount; row++){
            for(int col = 1; col <= colCount; col++){
                DailyTrainSeat trainSeat = new DailyTrainSeat();
                Date now = new Date();
                trainSeat.setId(idStrUtils.snowFlakeLong());
                trainSeat.setTrainCode(trainCode);
                trainSeat.setDate(date);
                trainSeat.setCarriageIndex(carriageVoIndex);
                trainSeat.setCol(StringTool.numberToStringByEnum(seatType, col));
                trainSeat.setRow(String.valueOf(row));
                trainSeat.setCarriageSeatIndex((row - 1) * colCount + col);
                trainSeat.setSeatType(seatType);
                trainSeat.setCreateTime(now);
                trainSeat.setUpdateTime(now);

                trainSeat.setSell(sell.toString());
                dailySeatService.insertSelective(trainSeat);
            }
        }

    }

    @Override
    public void batchInsertCarriage(List<DailyTrainCarriage> dailyTrainCarriages) {
        if(dailyTrainCarriages.isEmpty()){
            return;
        }
        logger.info("插入的车厢数据：【{}】", JSON.toJSON(dailyTrainCarriages));
        for (DailyTrainCarriage dt : dailyTrainCarriages) {
            if (dt.getId() == null){
                dt.setId(idStrUtils.snowFlakeLong());
            }
            if(dt.getCreateTime() == null){
                dt.setCreateTime(new Date());
            }
            if(dt.getUpdateTime() == null){
                dt.setUpdateTime(new Date());
            }
            dailyCarriageMapper.insertSelective(dt);
        }
    }

    @Override
    public void batchDelCarriageByCondition(DailyCarriageQuery query) {
        if(query == null){
            return;
        }
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        if(StrUtil.isNotBlank(query.getTrainCode())){
            criteria.andTrainCodeEqualTo(query.getTrainCode());
        }
        if(StrUtil.isNotBlank(query.getSeatType())){
            criteria.andSeatTypeEqualTo(query.getSeatType());
        }
        if(query.getDate() != null ){
            criteria.andDateEqualTo(query.getDate());
        }
        if(criteria.isValid()) {
            int i = dailyCarriageMapper.deleteByExample(dailyTrainCarriageExample);
            logger.info("批量删除每日车厢数据:【{}】", i);

        }
    }

    // 生成某日车厢数据
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result creatDayCarriage(DailyCarriageVo vo) {
        if (vo == null){
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        logger.info("开始生成{}日，数据", vo.getDate());
        Date date = vo.getDate();
        String trainCode = vo.getTrainCode();

        // 查询每日火车数据，根据火车数据查询基础车厢数据
        DailyTrainQuery dailyTrainQuery = new DailyTrainQuery();
        if (StrUtil.isNotBlank(trainCode)) {
            dailyTrainQuery.setCode(trainCode);
        }
        dailyTrainQuery.setDate(date);
        List<DailyTrain> dailyTrains = dailyTrainService.selectDTrainByCondition(dailyTrainQuery);
        logger.info("查询到的每日火车数据:【{}】", JSON.toJSONString(dailyTrains));


        List<TrainCarriage> allCarriages = new ArrayList<>();
        for (DailyTrain dailyTrain : dailyTrains) {
            TrainCarriage trainCarriage = new TrainCarriage();
            trainCarriage.setTrainCode(dailyTrain.getCode());
            List<TrainCarriage> trainCarriages = carriageService.selectAllCarriageWithCondition(trainCarriage);
            if(trainCarriages.isEmpty()){
                continue;
            }
            allCarriages.addAll(trainCarriages);
        }

        if(allCarriages.isEmpty()){
            return Result.error().message(StringTool.timeToStringByFormat(new DateTime(date)) + "日期暂无开放火车");
        }

        // 根据日期先删除每日车厢数据
        DailyTrainCarriageExample dailyTrainCarriageExample = new DailyTrainCarriageExample();
        DailyTrainCarriageExample.Criteria criteria = dailyTrainCarriageExample.createCriteria();
        criteria.andDateEqualTo(date);
        if(criteria.isValid()) {
            dailyCarriageMapper.deleteByExample(dailyTrainCarriageExample);
        }

        logger.info("查寻到可以添加基础数据是:【{}】", JSON.toJSONString(allCarriages));
        Date now = new Date();
        // 生成每日车厢数据
        allCarriages.forEach((item) ->{
            DailyTrainCarriage dailyTrainCarriage = new DailyTrainCarriage();
            BeanUtils.copyProperties(item, dailyTrainCarriage);
            dailyTrainCarriage.setId(idStrUtils.snowFlakeLong());
            dailyTrainCarriage.setCreateTime(now);
            dailyTrainCarriage.setUpdateTime(now);
            dailyTrainCarriage.setDate(date);
            dailyCarriageMapper.insertSelective(dailyTrainCarriage);
        });
        return Result.ok();
    }

    @Override
    public List<DailyTrainCarriage> selectAllCarriageByCondition(DailyTrainCarriage dtc) {
        DailyTrainCarriageExample example = new DailyTrainCarriageExample();
        if(dtc != null){
            DailyTrainCarriageExample.Criteria criteria = example.createCriteria();
            if(dtc.getId() != null){
                criteria.andIdEqualTo(dtc.getId());
            }
            if (StrUtil.isNotBlank(dtc.getTrainCode())) {
                criteria.andTrainCodeEqualTo(dtc.getTrainCode());
            }
            if(StrUtil.isNotBlank(dtc.getSeatType())){
                criteria.andSeatTypeEqualTo(dtc.getSeatType());
            }
            if(dtc.getDate() != null){
                criteria.andDateEqualTo(dtc.getDate());
            }
        }
        example.setOrderByClause("train_code asc");
        return dailyCarriageMapper.selectByExample(example);
    }

    @Override
    public int delDCarriageBeforeNow(Date date) {
        if(date == null || date.after(DateTime.now())){
            return 0;
        }
        DailyTrainCarriageExample example = new DailyTrainCarriageExample();
        DailyTrainCarriageExample.Criteria criteria = example.createCriteria();
        criteria.andDateLessThan(date);
        int count = dailyCarriageMapper.deleteByExample(example);
        return count;
    }

}
