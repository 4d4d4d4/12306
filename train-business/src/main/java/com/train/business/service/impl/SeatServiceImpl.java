package com.train.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.train.business.mapper.TrainMapper;
import com.train.business.mapper.TrainSeatMapper;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.domain.TrainSeat;
import com.train.common.base.entity.query.SeatQuery;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.query.TrainExample;
import com.train.common.base.entity.query.TrainSeatExample;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.SeatColVo;
import com.train.common.base.entity.vo.SeatVo;
import com.train.common.base.service.CarriageService;
import com.train.common.base.service.SeatService;
import com.train.common.enums.SeatColumnEnum;
import com.train.common.enums.SeatTypeEnum;
import com.train.common.resp.Result;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车座业务实现类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/18 下午8:54</li>
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
public class SeatServiceImpl implements SeatService {
    @Autowired
    private TrainSeatMapper seatMapper;

    @Autowired
    private IdStrUtils idUtils;
    @DubboReference(version = "1.0.0", check = false)
    private CarriageService carriageService;

    /**
     *
     * @param trainCarriage 座位所在的车厢信息
     * @param seatVo 座位信息
     * @return
     */
    @Override
    public Result addSeat(TrainCarriage trainCarriage, SeatVo seatVo) {
        if(trainCarriage == null){
            return Result.error().message("添加车座失败，未找到对应车厢");
        }
        // 获取当前车座信息
        String seatType = seatVo.getSeatType();
        Integer row = Integer.parseInt(seatVo.getRow());
        Integer col = Integer.parseInt(seatVo.getCol());
        Integer carriageSeatIndex = seatVo.getCarriageSeatIndex();
        // 校核当作座位类型和车厢座位类型是否一一致
        if(!StrUtil.equals(seatType, trainCarriage.getSeatType())){
            return Result.error().message("添加座位失败，添加的座位类型与车厢类型不符");
        }
        // 获取当前车厢行列数
        Integer colCount = trainCarriage.getColCount();
        Integer rowCount = trainCarriage.getRowCount();
        if(colCount<0 || colCount>col) {
            return Result.error().message("添加座位失败，添加的座位列数异常");
        }
        if(rowCount<0 || rowCount>row){
            return Result.error().message("添加座位失败，添加的座位排数异常");
        }
        Integer seatCount = trainCarriage.getSeatCount();
        if(carriageSeatIndex >  seatCount || carriageSeatIndex < 0){
            return Result.error().message("添加座位失败，添加的座号异常");
        }
        TrainSeat trainSeat = new TrainSeat();
        BeanUtils.copyProperties(seatVo, trainSeat);
        Date now = new Date();
        if(seatVo.getCreateTime() == null){
            trainSeat.setCreateTime(now);
        }
        if(seatVo.getUpdateTime() == null){
            trainSeat.setUpdateTime(now);
        }
        trainSeat.setId(idUtils.snowFlakeLong());
        seatMapper.insert(trainSeat);
        return Result.ok();
    }

    @Override
    public void delSeats(List<Long> ids) {
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.or().andIdIn(ids);
        seatMapper.deleteByExample(trainSeatExample);
    }

    @Override
    public Result editSeat(TrainCarriage trainCarriage,TrainSeat trainSeat) {
        if(trainCarriage == null){
            return Result.error().message("修改车座失败，未找到对应车厢");
        }
        // 获取当前车座信息
        String seatType = trainSeat.getSeatType();
        Integer row = Integer.parseInt(trainSeat.getRow());
        Integer col = Integer.parseInt(trainSeat.getCol());
        Integer carriageSeatIndex = trainSeat.getCarriageSeatIndex();
        // 校核当作座位类型和车厢座位类型是否一一致
        if(!StrUtil.equals(seatType, trainCarriage.getSeatType())){
            return Result.error().message("修改座位失败，修改的座位类型与车厢类型不符");
        }
        // 获取当前车厢行列数
        Integer colCount = trainCarriage.getColCount();
        Integer rowCount = trainCarriage.getRowCount();
        if(colCount<0 || colCount>col) {
            return Result.error().message("修改座位失败，修改的座位列数异常");
        }
        if(rowCount<0 || rowCount>row){
            return Result.error().message("修改座位失败，修改的座位排数异常");
        }
        Integer seatCount = trainCarriage.getSeatCount();
        if(carriageSeatIndex >  seatCount || carriageSeatIndex < 0){
            return Result.error().message("修改座位失败，修改的座号异常");
        }
        trainSeat.setUpdateTime(new Date());
        seatMapper.updateByPrimaryKey(trainSeat);
        return Result.ok();
    }

    @Override
    public PaginationResultVo<TrainSeat> selectPageByCondition(SeatQuery seatQuery) {
        PaginationResultVo<TrainSeat> result = new PaginationResultVo<>();
        if (seatQuery == null){
            return  result;
        }
        Integer currentPage = seatQuery.getCurrentPage() > 0 ? seatQuery.getCurrentPage() : 1;
        Integer pageSize = seatQuery.getPageSize() >= 5 ? seatQuery.getPageSize() : SimplePage.PAGE_20;
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        Integer totalCount = Math.toIntExact(seatMapper.countByExample(trainSeatExample));

        SimplePage simplePage = new SimplePage(totalCount, currentPage, pageSize);

        String trainCode = seatQuery.getTrainCode();
        Integer carriageIndex = seatQuery.getCarriageIndex();
        String seatType = seatQuery.getSeatType();
        trainSeatExample.clear();
        if(StrUtil.isNotBlank(trainCode)){
            trainSeatExample.or().andTrainCodeLike(StringTool.concat(trainCode));
        }
        if(StrUtil.isNotBlank(seatType)){
            trainSeatExample.or().andSeatTypeEqualTo(seatType);
        }
        if(carriageIndex != null){
            trainSeatExample.or().andCarriageIndexEqualTo(carriageIndex);
        }
        PageHelper.startPage(currentPage, pageSize);
        List<TrainSeat> trainSeats = seatMapper.selectByExample(trainSeatExample);
        result.setCurrentPage(currentPage);
        result.setPageSize(pageSize);
        result.setPageCount(simplePage.getPageTotal());
        result.setDataCount(totalCount);
        result.setResult(trainSeats);
        return result;

    }

    @Override
    public List<SeatColVo> getRowIndex(String trainCode, String carriageIndex, String col) {
         List<SeatColVo> result  = new ArrayList<>();
        if(StrUtil.isBlank(trainCode) || StrUtil.isBlank(col) || StrUtil.isBlank(carriageIndex)){
            return result;
        }
        Integer index = Integer.parseInt(carriageIndex);
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.or()
                .andTrainCodeEqualTo(trainCode)
                .andCarriageIndexEqualTo(index)
                .andColEqualTo(col);
        trainSeatExample.setOrderByClause("row");
        TrainExample trainExample = new TrainExample();
        trainExample.or().andCodeLike(trainCode);
        TrainCarriage trainCarriage = carriageService.selectByTrainCodeAndCarriageIndex(trainCode, index);
        Integer rowCount = trainCarriage.getRowCount();
        List<Integer> rows = IntStream.rangeClosed(1, rowCount).boxed().toList();
        List<TrainSeat> trainSeats = seatMapper.selectByExample(trainSeatExample);
        if(trainSeats.isEmpty()){
            rows.forEach((i) -> {
                SeatColVo seatColVo = new SeatColVo();
                seatColVo.setValue(String.valueOf(i));
                seatColVo.setDisabled(true);
                result.add(seatColVo);
            });
        }else {
            trainSeats.forEach(((item) ->{
                SeatColVo seatColVo = new SeatColVo();
                seatColVo.setValue(item.getRow());
                seatColVo.setDisabled(rows.contains(Integer.parseInt(item.getRow())));
                result.add(seatColVo);
            }));
        }
        return result;
    }

    @Override
    public void insertTrainSeat(TrainSeat trainSeat) {
        seatMapper.insert(trainSeat);
    }

    // 根据火车编码、车厢序号删除座位
    @Override
    public void delSeatsByCarriage(String trainCode, Integer carriageVoIndex) {
        TrainSeatExample trainSeatExample = new TrainSeatExample();
        trainSeatExample.or().andTrainCodeEqualTo(trainCode).andCarriageIndexEqualTo(carriageVoIndex);
        seatMapper.deleteByExample(trainSeatExample);

    }
}
