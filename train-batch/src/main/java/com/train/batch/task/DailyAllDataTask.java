package com.train.batch.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.EnumUtil;
import com.alibaba.fastjson.JSON;
import com.train.batch.annotation.TaskDesc;
import com.train.common.base.entity.domain.*;
import com.train.common.base.entity.query.DailyCarriageQuery;
import com.train.common.base.entity.query.DailyTrainQuery;
import com.train.common.base.service.*;
import com.train.common.enums.SeatTypeEnum;
import com.train.common.enums.TrainTypeEnum;
import com.train.common.utils.CommonUtils;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboReference;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : </dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/12/1 下午5:41</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@DisallowConcurrentExecution
@TaskDesc(value = "每日自动化生成所有日常数据任务", order = 0)
@Component
public class DailyAllDataTask implements Job {
    private static final Logger log = LoggerFactory.getLogger(DailyTrainTask.class);
    @DubboReference(version = "1.0.0", check = false, timeout = -1)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false, timeout = -1)
    private TrainService trainService;
    @Value("${business.daily.train.timeCount}")
    private Integer timeCount; // 每次生成的数据量/天
    @Autowired
    private IdStrUtils idStrUtils;
    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;
    @DubboReference(version = "1.0.0", check = false)
    private CarriageService carriageService;
    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dailySeatService;
    @DubboReference(version = "1.0.0", check = false)
    private SeatService seatService;
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @DubboReference(version = "1.0.0", check = false, timeout = -1)
    private DailyTrainStationService dailyTrainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTicketService dailyTicketService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 执行任务之前删除所有缓存
        String prefix = "train_";
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        JobKey jobKey = context.getJobDetail().getKey();
        Date now = new Date();
        try {
            if (creatDailyStation(now)) return;

            if (createDailyTrain(now)) return;
            createDailyCrriage(now);
            if (createDailySeat(now)) return;
            ceateDailyTicket(now);
        } catch (Exception e) {
            log.info("生成全部数据任务失败：{}", e.getMessage());
        } finally {
            long diffInMillies = DateTime.now().getTime() - now.getTime();
            // 将毫秒差值转换为秒
            long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillies);
            CompletableFuture<Void> future = TaskCompletionManager.getTask(jobKey);
            if (future != null) {
                future.complete(null); // 标记任务完成
                TaskCompletionManager.removeTask(jobKey); // 移除记录
            }
            log.info("任务结束所需时间为:{}s", diffInSeconds);

        }
    }


    private boolean creatDailyStation(Date now) {
        log.info("生成火车车站数据任务开始");
        // 获取所有车站基础数据
        List<TrainStation> trainStationList = trainStationService.selectAllTStationByCondition(null);
        if (trainStationList.isEmpty()) {
            log.info("生成火车车站任务结束，基础数据为空");
            return true;
        }
        DateTime time = DateTime.now();
        List<DailyTrainStation> insertList = new ArrayList<>();
        for (int i = 0; i < timeCount; i++) {
            // 根据日期删除数据
            DailyTrainStation trainStation = new DailyTrainStation();
            trainStation.setDate(time);
            dailyTrainStationService.batchDelTStationByCondition(trainStation);
            Date clone = (Date) time.clone();
            // 开始添加数据
            trainStationList.forEach((item) -> {
                DailyTrainStation dailyTrainStation = new DailyTrainStation();
                BeanUtils.copyProperties(item, dailyTrainStation);
                dailyTrainStation.setDate(clone);
                dailyTrainStation.setId(idStrUtils.snowFlakeLong());
                dailyTrainStation.setUpdateTime(now);
                dailyTrainStation.setCreateTime(now);
                insertList.add(dailyTrainStation);
            });
            time = time.offset(DateField.DAY_OF_YEAR, 1);
        }
        log.info("开始写入每日火车车站数据:【{}】", JSON.toJSONString(insertList));
        List<List<DailyTrainStation>> lists = CommonUtils.splitList(insertList);
        lists.forEach((item) -> {
            dailyTrainStationService.batchInsertDTStation(item);
        });

        log.info("每日火车车站任务结束");
        return false;
    }

    private void createDailyCrriage(Date now) {
        DateTime time = DateTime.now();
        log.info("开启生成每日车厢任务");
        // 先查询车厢基础数据
        List<TrainCarriage> carriageList = carriageService.selectAllCarriageWithCondition(null);

        for (int i = 0; i < timeCount; i++) {
            // 查询每日火车数据
            DailyTrainQuery dailyTrainQuery = new DailyTrainQuery();
            dailyTrainQuery.setDate(time.toSqlDate());
            List<DailyTrain> dailyTrains = dailyTrainService.selectDTrainByCondition(dailyTrainQuery);
            if (dailyTrains.isEmpty()) {
                continue;
            }
            DailyCarriageQuery dailyCarriageQuery = new DailyCarriageQuery();
            dailyCarriageQuery.setDate(dailyTrainQuery.getDate());
            // 删除time的车厢数据
            dailyCarriageService.batchDelCarriageByCondition(dailyCarriageQuery);
            for (DailyTrain train : dailyTrains) {
                // 根据每日火车数据判断哪些车厢可以添加
                List<TrainCarriage> list = carriageList.stream().filter((item) -> item.getTrainCode().equals(train.getCode())).toList();
                if (list.isEmpty()) {
                    continue;
                }
                List<DailyTrainCarriage> dailyTrainCarriages = new ArrayList<>();
                for (TrainCarriage trainCarriage : list) {
                    // 构造添加数据进行添加
                    DailyTrainCarriage dailyTrainCarriage = new DailyTrainCarriage();
                    BeanUtils.copyProperties(trainCarriage, dailyTrainCarriage);
                    dailyTrainCarriage.setDate(train.getDate());
                    dailyTrainCarriage.setId(idStrUtils.snowFlakeLong());
                    dailyTrainCarriage.setCreateTime(now);
                    dailyTrainCarriage.setUpdateTime(now);
                    dailyTrainCarriages.add(dailyTrainCarriage);
                }
                log.info("写入每日车厢数据:【{}】", JSON.toJSON(dailyTrainCarriages));
                dailyCarriageService.batchInsertCarriage(dailyTrainCarriages);
            }

            time = time.offset(DateField.DAY_OF_YEAR, 1);
        }
        log.info("生成每日车厢任务结束");
    }

    private boolean createDailyTrain(Date now) {
        DateTime time = DateTime.now();

        log.info("开始生成每日火车数据");
        // 查询基本数据
        List<Train> trains = trainService.selectTrainsByCode("");
        // 根据基本数据更新每日数据
        if (trains.isEmpty()) {
            log.info("每日火车任务结束， 无火车基础数据");
            return true;
        }
        for (int i = 0; i < timeCount; i++) {
            DailyTrainQuery dailyTrainQuery = new DailyTrainQuery();
            dailyTrainQuery.setDate(time);
//            List<DailyTrain> dTrains =  dailyTrainService.selectDTrainByCondition(dailyTrainQuery);
            // 删除重复数据
            dailyTrainService.delDTrainByCondition(dailyTrainQuery);
            DateTime finalTime = time;
            // 生成数据
            List<DailyTrain> insertList = trains.stream().map((item) -> {
                DailyTrain dailyTrain = new DailyTrain();
                BeanUtils.copyProperties(item, dailyTrain);
                dailyTrain.setId(idStrUtils.snowFlakeLong());
                dailyTrain.setDate(finalTime.toSqlDate());
                dailyTrain.setCreateTime(now);
                dailyTrain.setUpdateTime(now);
                return dailyTrain;
            }).toList();
//            insertList = insertList.stream().skip(1142).toList();
            if (insertList.size() > 200) {
                for (int j = 0; j < insertList.size(); j += 200) {
                    int end = Math.min(j + 200, insertList.size());
                    List<DailyTrain> dailyTrains = insertList.subList(j, end);
                    dailyTrainService.batchInsertDTrainData(dailyTrains);
                }
            } else {
                dailyTrainService.batchInsertDTrainData(insertList);
            }
            time = time.offset(DateField.DAY_OF_YEAR, 1); // 生成下一天数据
        }
        log.info("生成每日火车数据任务结束");
        return false;
    }

    private boolean createDailySeat(Date now) {
        DateTime time = DateTime.now();

        log.info("开始生成基础车座数据");
        // 查询基础车座数据
        TrainSeat trainSeat = new TrainSeat();
        List<TrainSeat> seats = seatService.selectAllSeatByCondition(trainSeat);
        log.info("查询到的基础车座数据:【{}】", JSON.toJSONString(seats));
        if (seats.isEmpty()) {
            log.info("每日车座数据任务结束，基础数据为空");
            return true;
        }

        for (Integer i = 0; i < timeCount; i++) {
            // 查询每日车厢数据，根据已有车厢分配车座
            DailyTrainCarriage dailyTrainCarriage = new DailyTrainCarriage();
            dailyTrainCarriage.setDate(time);
            List<DailyTrainCarriage> carriageList2 = dailyCarriageService.selectAllCarriageByCondition(dailyTrainCarriage);
            log.info("查询到的日期为：【{}】的车厢数据:【{}】", time, carriageList2);
            if (carriageList2.isEmpty()) {
                continue;
            }
            List<DailyTrainSeat> insertSeats = new ArrayList<>();

            for (DailyTrainCarriage trainCarriage : carriageList2) {
                String trainCode = trainCarriage.getTrainCode();
                Date date = trainCarriage.getDate();
                Integer index = trainCarriage.getIndex();
                DailyTrainSeat dailyTrainSeat = new DailyTrainSeat();
                dailyTrainSeat.setDate(date);
                dailyTrainSeat.setCarriageIndex(index);
                dailyTrainSeat.setTrainCode(trainCode);
                String seatType = trainCarriage.getSeatType();
                dailyTrainSeat.setSeatType(seatType);
                dailySeatService.batchDelSeatByCondition(dailyTrainSeat);

                // 新增每日车座
                Integer rowCount = trainCarriage.getRowCount(); // 行 数字
                Integer colCount = trainCarriage.getColCount(); // 列 字母
                for (int col = 1; col <= colCount; col++) {
                    dailyTrainSeat.setCol(StringTool.numberToStringByEnum(seatType, col));
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
                        for (int t = 1; t < trainStations.size(); t++) {
                            sell.append("0");
                        }
                        insertSeat.setSell(sell.toString());
                        insertSeat.setCreateTime(now);
                        insertSeat.setUpdateTime(now);
                        insertSeats.add(insertSeat);
                    }
                }
            }
            log.info("需要更新的每日车座数据:【{}】", JSON.toJSONString(insertSeats));
            List<List<DailyTrainSeat>> lists = CommonUtils.splitList(insertSeats);
            for (List<DailyTrainSeat> list : lists) {
                log.info("开始插入部分每日车座数据:【{}】", JSON.toJSONString(list));
                dailySeatService.batchInsertDSeat(list);
            }
            time = time.offset(DateField.DAY_OF_YEAR, 1);
        }
        log.info("每日车座任务结束");
        return false;
    }

    private void ceateDailyTicket(Date now) {
        log.info("开始生成日常余票");
        // 查询所有每日车座数据 (只有开放有车座的车次才是投入运营的)
        List<DailyTrainSeat> dailyTrainSeats = dailySeatService.selectDSeatByCondition(null);
        List<DailyTrainStation> dailyTrainStations = dailyTrainStationService.selectAllDTStationByCondition(null);
        List<DailyTrain> dailyTrains = dailyTrainService.selectDTrainByCondition(null);
        log.info("查询到的火车数据大小:{}，车站数据大小:{}，车座数据量：{}", dailyTrains.size(), dailyTrainStations.size(), dailyTrainSeats.size());
        if (dailyTrains.isEmpty() || dailyTrainSeats.isEmpty() || dailyTrainStations.isEmpty()) {
            log.info("暂无火车开放");
            return;
        }
        List<DailyTrainTicket> insertList = new ArrayList<>();
        for (DailyTrain dailyTrain : dailyTrains) {
            String code = dailyTrain.getCode();
            Date date = dailyTrain.getDate();
            if (date.before(CommonUtils.Y_M_D_H_m_STOY_M_D(now))) {
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
            if (trainStations.isEmpty()) {
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
                    List<DailyTrainSeat> seats2 = dailyTrainSeats.stream().filter((item) -> item.getTrainCode().equals(code) && item.getDate().equals(date)).toList();
                    if (seats2.isEmpty()) {
                        continue;
                    }
                    DailyTrainStation endTrainStation = trainStations.get(j);
                    distance = distance.add(endTrainStation.getKm());

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
                    fillInTicketInformation(dailyTrainTicket, seats2, distance);

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
        if (seats.isEmpty()) {
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
        BigDecimal traInTypeFactor = trainType.getPrice(); // 火车类型价格因数
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
            if (!typeCountMap.containsKey(seatType)) {
                typeCountMap.put(seatType, 0);
            }
            String sell = seat.getSell();
            // 根据站序判断是否可以购买
            boolean canSell = CommonUtils.canSell(startIndex, endIndex, sell);
            if (canSell) {
                typeCountMap.put(seatType, typeCountMap.get(seatType) + 1);
            }
        }
        Integer ydz = typeCountMap.get(SeatTypeEnum.YDZ.getCode());
        Integer edz = typeCountMap.get(SeatTypeEnum.EDZ.getCode());
        Integer ywz = typeCountMap.get(SeatTypeEnum.YWZ.getCode());
        Integer rwz = typeCountMap.get(SeatTypeEnum.RWZ.getCode());
        dailyTrainTicket.setYdz(ydz == null ? -1 : ydz);
        dailyTrainTicket.setEdz(edz == null ? -1 : edz);
        dailyTrainTicket.setYw(ywz == null ? -1 : ywz);
        dailyTrainTicket.setRw(rwz == null ? -1 : rwz);
    }
}
