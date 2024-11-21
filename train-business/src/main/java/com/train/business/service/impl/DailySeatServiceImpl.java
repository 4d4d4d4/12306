package com.train.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.train.business.mapper.DailyTrainSeatMapper;
import com.train.common.base.entity.domain.DailyTrainCarriage;
import com.train.common.base.entity.domain.DailyTrainSeat;
import com.train.common.base.entity.domain.TrainSeat;
import com.train.common.base.entity.domain.TrainStation;
import com.train.common.base.entity.query.DailySeatQuery;
import com.train.common.base.entity.query.DailyTrainExample;
import com.train.common.base.entity.query.DailyTrainSeatExample;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.vo.DailySeatVo;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车座接口实现</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/31 下午5:44</li>
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
public class DailySeatServiceImpl extends BaseService implements DailySeatService {
    private static final Logger log = LoggerFactory.getLogger(DailySeatServiceImpl.class);
    @Autowired
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    @Autowired
    private IdStrUtils idStrUtils;
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private SeatService seatService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;
    @Override
    public PaginationResultVo<DailyTrainSeat> queryAllDSeat(DailySeatQuery q) {
        if (q == null) {
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        PaginationResultVo<DailyTrainSeat> result = new PaginationResultVo<>();
        // 分页条件
        Integer currentPage = q.getCurrentPage();
        Integer pageSize = q.getPageSize();
        if (currentPage != null || pageSize != null) {
            currentPage = currentPage == null || currentPage < 1 ? 1 : currentPage;
            pageSize = pageSize == null || pageSize < 5 ? SimplePage.PAGE_20 : pageSize;
            PageHelper.startPage(currentPage, pageSize);
        }
        // 查询条件
        Date date = q.getDate();
        String trainCode = q.getTrainCode();

        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
        if(date != null){
            criteria.andDateEqualTo(date);
        }
        if(trainCode != null){
            if(Objects.equals(trainCode, "null")) {
                trainCode = "";
            }
            criteria.andTrainCodeLike(StringTool.concat(trainCode));
        }
        List<DailyTrainSeat> dailyTrainSeats = dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
        PageInfo<DailyTrainSeat> page = new PageInfo<>(dailyTrainSeats);

        result.setResult(dailyTrainSeats);
        result.setPageCount(page.getPages());
        result.setCurrentPage(page.getPageNum());
        result.setDataCount((int) page.getTotal());
        result.setPageCount(page.getPages());

        return result;
    }

    @Override
    public Result batchDelSeat(List<Long> ids) {
        if(ids.isEmpty()){
            return Result.error().message("删除失败，数据为空、请重新操作");
        }
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        dailyTrainSeatExample.or().andIdIn(ids);
        int i = dailyTrainSeatMapper.deleteByExample(dailyTrainSeatExample);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result insertDSeat(DailySeatVo seatVo) {
        if(seatVo == null){
            return Result.error().message("数据为空，添加失败");
        }
        DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
        BeanUtils.copyProperties(seatVo, dailyTrainSeat);
        if(dailyTrainSeat.getCreateTime() == null){
            dailyTrainSeat.setCreateTime(new Date());
        }
        if(StrUtil.isNotBlank(dailyTrainSeat.getRow())){
            dailyTrainSeat.setRow(StringTool.formatRow(dailyTrainSeat.getRow()));
        }
        if(dailyTrainSeat.getUpdateTime() == null){
            dailyTrainSeat.setUpdateTime(new Date());
        }
        dailyTrainSeat.setId(idStrUtils.snowFlakeLong());

        // 初始化售卖情况
        String trainCode = seatVo.getTrainCode();
        List<TrainStation> trainStations = trainStationService.getTrainStationByTrainCode(trainCode);
        StringBuilder sell = new StringBuilder();
        // 若经停4站则为000 每个零分别代表 1-2 2-3 3-4 站该车位是否可以购买 0为可以购买 1为不可以
        for(int i = 1; i < trainStations.size(); i++){
            sell.append("0");
        }
        dailyTrainSeat.setSell(sell.toString());
        int insertSelective = dailyTrainSeatMapper.insertSelective(dailyTrainSeat);
        return insertSelective > 0 ? Result.ok() : Result.error();
    }

    @Override
    public Result updateDSeat(DailyTrainSeat seat) {
        if(seat == null || seat.getId() == null){
            return Result.error().message("修改失败，数据为空");
        }
        if(StrUtil.isNotBlank(seat.getRow())){
            seat.setRow(StringTool.formatRow(seat.getRow()));
        }
        seat.setUpdateTime(new Date());
        int i = dailyTrainSeatMapper.updateByPrimaryKeySelective(seat);
        return i > 0 ? Result.ok() : Result.error();
    }

    @Override
    public void insertSelective(DailyTrainSeat trainSeat) {
        if(StrUtil.isNotBlank(trainSeat.getRow())){
            trainSeat.setRow(StringTool.formatRow(trainSeat.getRow()));
        }
     dailyTrainSeatMapper.insertSelective(trainSeat);
    }

    @Override
    public void batchDelSeatByCondition(DailyTrainSeat dailyTrainSeat) {
        if (dailyTrainSeat == null){
            return;
        }
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
        if (dailyTrainSeat.getDate() != null) {
            criteria.andDateEqualTo(dailyTrainSeat.getDate());
        }
        if(StrUtil.isNotBlank(dailyTrainSeat.getTrainCode())){
            criteria.andTrainCodeEqualTo(dailyTrainSeat.getTrainCode());
        }
        if(dailyTrainSeat.getCarriageSeatIndex()!=null){
            criteria.andCarriageIndexEqualTo(dailyTrainSeat.getCarriageSeatIndex());
        }
        if(criteria.isValid()){
            int i = dailyTrainSeatMapper.deleteByExample(dailyTrainSeatExample);
            log.info("批量删除每日车座数据:【{}】", i);

        }
    }

    @Override
    public Result createDSeat(DailySeatVo vo) {
        if(vo == null){
            throw new BusinessException(ResultStatusEnum.CODE_500.getCode(), "生成参数为空");
        }
        Date time = vo.getDate();
        log.info("生成日期为：【{}】的车座数据开始", time);
        // 查询基础车座数据
        List<TrainSeat> trainSeats = seatService.selectAllSeatByCondition(null);
        if(trainSeats.isEmpty()){
            log.error("每日生成车座数据结束，基础数据为空");
            return Result.error().message("车座基础数据异常");
        }
        // 查询每日车厢数据 生成符合时间条件的车厢中的车座
        DailyTrainCarriage dailyTrainCarriage = new DailyTrainCarriage();
        dailyTrainCarriage.setDate(time);
        List<DailyTrainCarriage> dailyTrainCarriages = dailyCarriageService.selectAllCarriageByCondition(dailyTrainCarriage);
        if(dailyTrainCarriages.isEmpty()){
            return Result.error().message("日期：【" + time  +"】 暂无开放车厢数据");
        }
        List<DailyTrainSeat> insertSeats = new ArrayList<>();
        for (DailyTrainCarriage trainCarriage : dailyTrainCarriages) {
            String trainCode = trainCarriage.getTrainCode();
            Date date = trainCarriage.getDate();
            Integer index = trainCarriage.getIndex();
            DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
            dailyTrainSeat.setDate(date);
            dailyTrainSeat.setCarriageIndex(index);
            dailyTrainSeat.setTrainCode(trainCode);
            String seatType = trainCarriage.getSeatType();
            dailyTrainSeat.setSeatType(seatType);
            batchDelSeatByCondition(dailyTrainSeat);
            Date now = new Date();

            // 新增每日车座
            Integer rowCount = trainCarriage.getRowCount(); // 行 数字
            Integer colCount = trainCarriage.getColCount(); // 列 字母
            for (int col = 1; col <= colCount; col++) {
                dailyTrainSeat.setCol(StringTool.numberToStringByEnum(seatType,col));
                for (int row = 1; row <= rowCount; row++) {
                    DailyTrainSeat insertSeat = new DailyTrainSeat();
                    BeanUtils.copyProperties(dailyTrainSeat, insertSeat);
                    int carriageSeatIndex = (row - 1) * colCount + col;
                    insertSeat.setRow(StringTool.formatRow(row));

                    insertSeat.setCarriageSeatIndex(carriageSeatIndex);

                    insertSeat.setId(idStrUtils.snowFlakeLong());

                    List<TrainStation> trainStations = trainStationService.getTrainStationByTrainCode(trainCode);
                    StringBuilder sell = new StringBuilder();
                    // 若经停4站则为000 每个零分别代表 1-2 2-3 3-4 站该车位是否可以购买 0为可以购买 1为不可以
                    for(int t = 1;t < trainStations.size(); t++){
                        sell.append("0");
                    }
                    insertSeat.setSell(sell.toString());
                    insertSeat.setCreateTime(now);
                    insertSeat.setUpdateTime(now);
                    insertSeats.add(insertSeat);
                }
            }
            List<List<DailyTrainSeat>> lists = spiltDataList(insertSeats);
            for (List<DailyTrainSeat> list : lists) {
                log.info("开始插入部分每日车座数据:【{}】", JSON.toJSONString(list));
                batchInsertDSeat(list);
            }
        }
        log.info("生成日期：【{}】的每日车座任务结束", time);
        return Result.ok();
    }

    @Override
    public void batchInsertDSeat(List<DailyTrainSeat> insertSeats) {
        log.info("开始插入每日车座，数据集:【{}】，数据集数量：【{}】", JSON.toJSONString(insertSeats), insertSeats.size());
        if(insertSeats.isEmpty()){
            log.error("插入每日车座任务结束：插入数据集为空");
        }
        insertSeats.forEach((item) ->{
            if(item.getId() == null){
                item.setId(idStrUtils.snowFlakeLong());
            }
            if(item.getDate() == null || item.getSell() == null || item.getSeatType() == null){
                log.error("【{}】数据插入失败，date、sell、seatType字段有空数据", JSON.toJSONString(item));
                return;
            }
            if(item.getCreateTime() == null){
                item.setCreateTime(new Date());
            }
            if(item.getUpdateTime() == null){
                item.setUpdateTime(new Date());
            }
            int i = dailyTrainSeatMapper.insertSelective(item);
            if(i > 0) log.info("【{}】数据插入成功", JSON.toJSONString(item));
        });
    }

    @Override
    public List<DailyTrainSeat> selectDSeatByCondition(DailyTrainSeat o) {
        DailyTrainSeatExample dailyTrainSeatExample = new DailyTrainSeatExample();
        if(o != null){
            DailyTrainSeatExample.Criteria criteria = dailyTrainSeatExample.createCriteria();
            if(o.getDate() != null){
                criteria.andDateEqualTo(o.getDate());
            }
            if(o.getId() != null){
                criteria.andIdEqualTo(o.getId());
            }
            if(StrUtil.isNotBlank(o.getTrainCode())){
                criteria.andTrainCodeLike(StringTool.concat(o.getTrainCode()));
            }
        }

        return dailyTrainSeatMapper.selectByExample(dailyTrainSeatExample);
    }

    /**
     * 根据查询条件查询所有每日车座，并且以车厢、列、排进行分组和排序
     * @param dailyTrainSeat
     * @return
     */
    @Override
    public List<DailyTrainSeat> selectDSeatByConditionExample(DailyTrainSeat dailyTrainSeat) {
        log.info("参数：【{}】", JSON.toJSONString(dailyTrainSeat));
        if(dailyTrainSeat == null || dailyTrainSeat.getTrainCode().isEmpty() || dailyTrainSeat.getDate() == null){
            return List.of();
        }
        java.sql.Date date = new java.sql.Date(dailyTrainSeat.getDate().getTime());
        String trainCode = dailyTrainSeat.getTrainCode();
        return dailyTrainSeatMapper.selectAllDSeatWithGroupAndOrder(trainCode, date);
    }
}
