package com.train.batch.task;

import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson.JSON;
import com.train.batch.annotation.TaskDesc;
import com.train.common.base.entity.domain.*;
import com.train.common.base.service.*;
import com.train.common.enums.SeatTypeEnum;
import com.train.common.enums.TrainTypeEnum;
import com.train.common.utils.CommonUtils;
import com.train.common.utils.IdStrUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日余票任务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/7 下午3:37</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@TaskDesc("每日车票任务")
@DisallowConcurrentExecution
@Component
public class DailyTicketTask implements Job {
    @DubboReference(version = "1.0.0", check = false)
    private DailyTicketService dailyTicketService;
    @DubboReference(version = "1.0.0", check = false,timeout = -1)
    private DailyTrainStationService dailyTrainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dailySeatService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;
    @Autowired
    private IdStrUtils idStrUtils;
    @Value("${business.daily.train.timeCount}")
    private Integer timeCount;
    private final static Logger log = LoggerFactory.getLogger(DailyTicketTask.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始生成日常余票");
        // 查询所有每日车座数据 (只有开放有车座的车次才是投入运营的)
        List<DailyTrainSeat> dailyTrainSeats = dailySeatService.selectDSeatByCondition(null);
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationService.selectAllDTStationByCondition(null);
        List<DailyTrain> dailyTrains = dailyTrainService.selectDTrainByCondition(null);
        log.info("查询到的火车数据大小:{}，车站数据大小:{}，车座数据量：{}",dailyTrains.size(), dailyTrainStations.size(), dailyTrainSeats.size());
        if(dailyTrains.isEmpty() || dailyTrainSeats.isEmpty() || dailyTrainStations.isEmpty()){
            log.info("暂无火车开放");
            return;
        }
        Date now = new Date();
        List<DailyTrainTicket> insertList = new ArrayList<>();
        for (DailyTrain dailyTrain : dailyTrains) {
            String code = dailyTrain.getCode();
            Date date = dailyTrain.getDate();
            if(date.before(CommonUtils.Y_M_D_H_m_STOY_M_D(now))){
                continue;
            }
            // 删除车票表中原有的数据

            dailyTicketService.batchDelTicketByDate(date);
            log.info("根据编码:{}和时间:{}条件筛选车站集合：【】", code, date);

            // 根据火车编码和时间查询所有火车车站数据 可以增加index字段的排序但在数据库中已经完成了排序
            List<DailyTrainStation> trainStations = dailyTrainStations.stream()
                    .filter((item) -> item.getTrainCode().equals(code) && item.getDate().equals(date))
                    .sorted(Comparator.comparing(DailyTrainStation::getTrainCode))
                    .toList();
            if(trainStations.isEmpty()){
                continue;
            }
            log.info("符合编码:{}和时间:{}条件的数据集合：【{}】", code, date, trainStations);
            for (int i = 0; i < trainStations.size(); i++) {
                DailyTrainStation startTrainStation = trainStations.get(i);
                String sname = startTrainStation.getName();
                String snamePinyin = startTrainStation.getNamePinyin();
                Integer sindex = startTrainStation.getIndex();
                Date startTime = startTrainStation.getOutTime();
                BigDecimal distance = BigDecimal.ZERO; // 始发站到终点站距离
                for (int j = i + 1; j < trainStations.size(); j++) {
                    List<DailyTrainSeat> seats = dailyTrainSeats.stream().filter((item) -> item.getTrainCode().equals(code) && item.getDate().equals(date)).toList();
                    if (seats.isEmpty()){
                        continue;
                    }
                    DailyTrainStation endTrainStation = trainStations.get(j);
                    distance =  distance.add(endTrainStation.getKm());

                    String ename = endTrainStation.getName();
                    String enamePinyin = endTrainStation.getNamePinyin();
                    Integer eindex = endTrainStation.getIndex();
                    Date endTime = endTrainStation.getInTime();
                    DailyTrainTicket dailyTrainTicket = new DailyTrainTicket();
                    dailyTrainTicket.setDate(date);

                    dailyTrainTicket.setStart(sname);
                    dailyTrainTicket.setStartPinyin(snamePinyin);
                    dailyTrainTicket.setStartIndex(sindex);
                    dailyTrainTicket.setStartTime(startTime);
                    dailyTrainTicket.setEnd(ename);
                    dailyTrainTicket.setEndPinyin(enamePinyin);
                    dailyTrainTicket.setEndIndex(eindex);
                    dailyTrainTicket.setEndTime(endTime);

                    dailyTrainTicket.setTrainCode(code);
                    dailyTrainTicket.setId(idStrUtils.snowFlakeLong());

                    // 计算座位信息 各个座位类型的票价和票数量
                    fillInTicketInformation(dailyTrainTicket, seats, distance);

                    dailyTrainTicket.setCreateTime(now);
                    dailyTrainTicket.setUpdateTime(now);

                    insertList.add(dailyTrainTicket);
                }
            }
        }
        log.info("要添加的数据是：【{}】", JSON.toJSONString(insertList.size()));
        CommonUtils.splitList(insertList).forEach((list) -> dailyTicketService.batchInsertList(list));
        log.info("车票数据初始化结束");

    }

    private void fillInTicketInformation(DailyTrainTicket dailyTrainTicket, List<DailyTrainSeat> seats, BigDecimal distance) {
        String trainCode = dailyTrainTicket.getTrainCode();
//        Date date = dailyTrainTicket.getDate();
//        List<DailyTrainSeat> seats = dailyTrainSeats.stream().filter((item) -> item.getTrainCode().equals(trainCode) && item.getDate().equals(date)).toList();
        if(seats.isEmpty()){
            dailyTrainTicket.setYdz(-1);
            dailyTrainTicket.setEdz(-1);
            dailyTrainTicket.setYw(-1);
            dailyTrainTicket.setRw(-1);
            dailyTrainTicket.setYwPrice(BigDecimal.ZERO);
            dailyTrainTicket.setRwPrice(BigDecimal.ZERO);
            dailyTrainTicket.setYdzPrice(BigDecimal.ZERO);
            dailyTrainTicket.setEdzPrice(BigDecimal.ZERO);
            return;
        }
        // 火车类型
        TrainTypeEnum trainType = EnumUtil.getBy(TrainTypeEnum.class, e -> trainCode.contains(e.getCode()));
        log.info("火车编码:{}的火车类型为：【{}】", trainCode, trainType);
        BigDecimal traInTypeFactor  = trainType.getPrice(); // 火车类型价格因数
        // 计算四种座位价格
        BigDecimal rwPrice = distance.multiply(traInTypeFactor).multiply(SeatTypeEnum.RWZ.getPrice());
        BigDecimal ywPrice = distance.multiply(traInTypeFactor).multiply(SeatTypeEnum.YWZ.getPrice());
        BigDecimal ydzPrice = distance.multiply(traInTypeFactor).multiply(SeatTypeEnum.YDZ.getPrice());
        BigDecimal edzPrice = distance.multiply(traInTypeFactor).multiply(SeatTypeEnum.EDZ.getPrice());
        dailyTrainTicket.setYwPrice(ywPrice);
        dailyTrainTicket.setRwPrice(rwPrice);
        dailyTrainTicket.setYdzPrice(ydzPrice);
        dailyTrainTicket.setEdzPrice(edzPrice);

        // 计算座位类型数量
        Integer startIndex = dailyTrainTicket.getStartIndex();
        Integer endIndex = dailyTrainTicket.getEndIndex();
        Map<String, Integer> typeCountMap = new ConcurrentHashMap<>();
        for (DailyTrainSeat seat : seats) {
            String seatType = seat.getSeatType();
            if(!typeCountMap.containsKey(seatType)){
                typeCountMap.put(seatType,0);
            }
            String sell = seat.getSell();
            // 根据站序判断是否可以购买
            boolean canSell = CommonUtils.canSell(startIndex, endIndex, sell);
            if(canSell){
                typeCountMap.put(seatType, typeCountMap.get(seatType) + 1);
            }
        }
        Integer ydz = typeCountMap.get(SeatTypeEnum.YDZ.getCode());
        Integer edz = typeCountMap.get(SeatTypeEnum.EDZ.getCode());
        Integer ywz = typeCountMap.get(SeatTypeEnum.YWZ.getCode());
        Integer rwz = typeCountMap.get(SeatTypeEnum.RWZ.getCode());
        dailyTrainTicket.setYdz(ydz == null ? -1 : ydz);
        dailyTrainTicket.setEdz(edz == null ? -1 :edz);
        dailyTrainTicket.setYw(ywz == null ? -1 : ywz);
        dailyTrainTicket.setRw(rwz == null ? -1 : rwz);
    }
}
