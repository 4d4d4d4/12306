package com.train.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.pagehelper.PageHelper;
import com.train.business.mapper.StationMapper;
import com.train.business.mapper.TrainCarriageMapper;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.domain.TrainSeat;
import com.train.common.base.entity.query.CarriageQuery;
import com.train.common.base.entity.query.SimplePage;
import com.train.common.base.entity.query.TrainCarriageExample;
import com.train.common.base.entity.query.TrainExample;
import com.train.common.base.entity.vo.CarriageIndexVo;
import com.train.common.base.entity.vo.CarriageVo;
import com.train.common.base.entity.vo.PaginationResultVo;
import com.train.common.base.entity.vo.TrainCodeVo;
import com.train.common.base.service.CarriageService;
import com.train.common.base.service.SeatService;
import com.train.common.base.service.TrainService;
import com.train.common.enums.CarriageIndexEnum;
import com.train.common.enums.SeatColumnEnum;
import com.train.common.enums.SeatTypeEnum;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.IdStrUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 车厢管理实现类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/17 下午3:56</li>
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
public class CarriageServiceImpl implements CarriageService {
    private static final Logger logger = LoggerFactory.getLogger(CarriageServiceImpl.class);

    @Autowired
    private TrainCarriageMapper carriageMapper;

    @DubboReference(version = "1.0.0", check = false)
    private TrainService trainService;

    @Autowired
    private IdStrUtils idStrUtils;
    @Qualifier("seatService")
    @Autowired
    private SeatService seatService;

    @Override
    @Transactional
    public String batchAdd(List<CarriageVo> carriageVoList) {
        List<String> result = new ArrayList<>();
        carriageVoList.forEach((item) -> {
            TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
            trainCarriageExample.clear();
            String trainCode = item.getTrainCode();
            Integer trainIndex = item.getIndex();
            String seatType = item.getSeatType();
            String str = "开始添加火车id为 " + trainCode + " 车厢序号为 " + trainIndex + "：";
            // 检查座位类型是否合规
            SeatTypeEnum seatTypeEnumByCode = SeatTypeEnum.getSeatTypeEnumByCode(seatType);
            if (seatTypeEnumByCode == null) {
                str = str + "添加失败，座位类型数据异常\n";
                result.add(str);
                return;
            }
            // 检查数据是否与数据库重复
            if (StrUtil.isBlank(trainCode) || trainIndex == null ) {
                str = str + "添加失败，关键数据为空\n";
                result.add(str);
                return;
            }
            if(trainIndex > 8 || trainIndex < 1){
                str = str + "添加失败，车厢序号应为1-8\n";
                result.add(str);
                return;
            }
            trainCarriageExample.or().andTrainCodeEqualTo(trainCode).andIndexEqualTo(trainIndex);
            long l = carriageMapper.countByExample(trainCarriageExample);
            if(l != 0){
                str = str + "添加失败，数据重复\n";
                result.add(str);
                return;
            }
            // 赋予雪花ID 创建时间 修改时间
            TrainCarriage trainCarriage = new TrainCarriage();
            Long id = idStrUtils.snowFlakeLong();
            BeanUtils.copyProperties(item, trainCarriage);
            trainCarriage.setId(id);
            if(trainCarriage.getCreateTime() == null){
                trainCarriage.setCreateTime(new Date());
            }
            if(trainCarriage.getUpdateTime() == null){
                trainCarriage.setUpdateTime(new Date());
            }
            // 执行添加操作
            carriageMapper.insertSelective(trainCarriage);
        });

        return result.isEmpty() ? "添加成功" : result.toString();
    }

    @Override
    public void batchDelete(List<Long> ids) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        if (ids.isEmpty()) {
            return;
        }
        trainCarriageExample.or().andIdIn(ids);
        carriageMapper.deleteByExample(trainCarriageExample);
    }

    @Override
    public Result updateCarriage(TrainCarriage trainCarriage) {
        if (trainCarriage == null) {
            logger.info("传入参数为null， 修改失败");
            return Result.error().message("传入参数为null，修改失败");
        }
        String trainCode = trainCarriage.getTrainCode();
        Integer trainIndex = trainCarriage.getIndex();
        int updates = 0;
        if (trainCarriage.getId() != null) { // 根据车厢id修改
            logger.info("通过id对车厢:{}进行修改", trainCarriage);
            updates = carriageMapper.updateByPrimaryKey(trainCarriage);
        } else if (StrUtil.isNotBlank(trainCode) && (trainIndex != null && trainIndex > 0)) {  // 根据火车编码及车厢序号修改
            TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
            logger.info("通过火车编码及车厢号码对车厢:{}进行修改", trainCarriage);
            trainCarriageExample.or().andTrainCodeEqualTo(trainCode).andIndexEqualTo(trainIndex);
            updates = carriageMapper.updateByExample(trainCarriage, trainCarriageExample);
        }

        return updates == 0 ? Result.error().message("未能找到车厢数据")
                : Result.ok().message("成功修改" + updates + "项数据");
    }

    @Override
    public PaginationResultVo<TrainCarriage> selectAllByConditionWithPage(CarriageQuery query) {
        // 声明构造查询条件类
        TrainCarriageExample example =  new TrainCarriageExample();
        example.clear();
        example.setDistinct(true);
        TrainCarriageExample.Criteria criteria = example.createCriteria();
        // 条件查询基础数据
        String trainCode = query.getTrainCode(); // 火车编码
        Integer trainIndex = query.getIndex(); // 车厢序号
        String seatType = query.getSeatType(); //  车厢座位类型
        // 填充查询条件
        if (StrUtil.isNotBlank(trainCode)) {
            criteria.andTrainCodeEqualTo(trainCode);
        }
        if (trainIndex != null && trainIndex > 0) {
            criteria.andIndexEqualTo(trainIndex);
        }
        if (StrUtil.isNotEmpty(seatType)) {
            criteria.andSeatTypeEqualTo(seatType);
        }
        example.or(criteria);
        Integer dataCount = (int) carriageMapper.countByExample(example);
        // 分页查询基础数据
        Integer currentPage = query.getCurrentPage();
        Integer pageSize = query.getPageSize() >= 5 ? query.getPageSize() : SimplePage.PAGE_20;
        SimplePage page = new SimplePage(dataCount, currentPage, pageSize);
        PageHelper.startPage(currentPage, pageSize);
        // 查询数据
        List<TrainCarriage> trainCarriages = carriageMapper.selectByExample(example);

        PaginationResultVo<TrainCarriage> result = new PaginationResultVo<>();
        result.setPageSize(pageSize);
        result.setCurrentPage(currentPage);
        result.setResult(trainCarriages);
        result.setPageCount(page.getPageTotal());
        result.setDataCount(page.getTotalCount());
        return result;
    }

    @Override
    public List<CarriageIndexVo> getAllCarriagesIndex(String trainCode) {
        ArrayList<CarriageIndexVo> result = new ArrayList<>();
        if(StrUtil.isBlank(trainCode)){
            return result;
        }
        // 根据火车编码查询车厢情况
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.or().andTrainCodeEqualTo(trainCode);
        List<TrainCarriage> trainCarriages = carriageMapper.selectByExample(trainCarriageExample);

        // 获取数据库存在的车厢序号
        List<Integer> indexList = trainCarriages.stream().map(TrainCarriage::getIndex).toList();
        Map<Integer, TrainCarriage> indexMap = trainCarriages.stream().collect(Collectors.toMap(TrainCarriage::getIndex, (item) -> item));
        for (CarriageIndexEnum en : CarriageIndexEnum.values()) {
            // 整合返回数据，默认每个火车固定有八个车厢 数据库中含有的车厢即为不可再用
            CarriageIndexVo carriageIndexVo = new CarriageIndexVo();
            carriageIndexVo.setValue(en.getIndex());
            carriageIndexVo.setLabel(en.getDesc());
            TrainCarriage trainCarriage = indexMap.get(en.getIndex());
            boolean disable = trainCarriages.isEmpty() || trainCarriage == null;
            carriageIndexVo.setDisable(disable);
            if(trainCarriage != null){
                carriageIndexVo.setSeatType(trainCarriage.getSeatType());
                carriageIndexVo.setRowCount(trainCarriage.getRowCount());
                carriageIndexVo.setColCount(trainCarriage.getColCount());
            }
            result.add(carriageIndexVo);
        }

        return result;
    }

    @Override
    public List<TrainCodeVo> getAllTrainByCode(String trainCode) {
        if(trainCode == null){
            trainCode = "";
        }
        List<Train> trains = trainService.selectTrainsByCode(trainCode);
        List<TrainCodeVo> result = trains.stream().map((item) -> {
            TrainCodeVo trainCodeVo = new TrainCodeVo();
            BeanUtils.copyProperties(item, trainCodeVo);
            return trainCodeVo;
        }).toList();
        return result;
    }

    @Override
    public TrainCarriage selectByTrainCodeAndCarriageIndex(String trainCode,Integer carriageIndex) {
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.or().andTrainCodeEqualTo(trainCode).andIndexEqualTo(carriageIndex);
        List<TrainCarriage> trainCarriages = carriageMapper.selectByExample(trainCarriageExample);
        if(trainCarriages.isEmpty()){
            return null;
        }
        return trainCarriages.get(0);
    }

    @Override
    @Transactional
    public void createCarriage(CarriageVo carriageVo) {
         String trainCode = carriageVo.getTrainCode();
        String seatType = carriageVo.getSeatType();
        Integer carriageVoIndex = carriageVo.getIndex();

        // 查询火车是否存在
        List<Train> trains = trainService.selectTrainsByCode(trainCode);
        if(trains.isEmpty()){
            throw new BusinessException(500, "系统错误，请核实火车编码是否正确");
        }
        // 查询车厢(若存在)获取车厢信息 填充车座信息
        TrainCarriageExample trainCarriageExample = new TrainCarriageExample();
        trainCarriageExample.or().andTrainCodeEqualTo(trainCode).andIndexEqualTo(carriageVoIndex).andSeatTypeEqualTo(seatType);
        List<TrainCarriage> trainCarriages = carriageMapper.selectByExample(trainCarriageExample);
        if(trainCarriages.isEmpty()){
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        // 清除数据库中旧车座
        seatService.delSeatsByCarriage(trainCode, carriageVoIndex);
        // 遍历车厢中所有座位新增车座
        TrainCarriage trainCarriage = trainCarriages.get(0);
        Integer rowCount = trainCarriage.getRowCount();
        Integer colCount = trainCarriage.getColCount();
        for(int row = 1; row <= rowCount; row++){
            for(int col = 1; col <= colCount; col++){
                TrainSeat trainSeat = new TrainSeat();
                Date now = new Date();
                trainSeat.setId(idStrUtils.snowFlakeLong());
                trainSeat.setTrainCode(trainCode);
                trainSeat.setCarriageIndex(carriageVoIndex);
                trainSeat.setCol(String.valueOf(col));
                trainSeat.setRow(String.valueOf(row));
                trainSeat.setCarriageSeatIndex((row - 1) * colCount + col);
                trainSeat.setSeatType(seatType);
                trainSeat.setCreateTime(now);
                trainSeat.setUpdateTime(now);
                seatService.insertTrainSeat(trainSeat);
            }
        }
    }
}
