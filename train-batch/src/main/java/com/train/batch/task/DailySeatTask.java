package com.train.batch.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.train.batch.annotation.TaskDesc;
import com.train.common.base.entity.domain.*;
import com.train.common.base.service.DailyCarriageService;
import com.train.common.base.service.DailySeatService;
import com.train.common.base.service.SeatService;
import com.train.common.base.service.TrainStationService;
import com.train.common.utils.CommonUtils;
import com.train.common.utils.IdStrUtils;
import com.train.common.utils.StringTool;
import org.apache.dubbo.config.annotation.DubboReference;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日车座任务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/3 下午10:26</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@TaskDesc(value = "生成三日车座任务",order = 3)
@DisallowConcurrentExecution
@Component
public class DailySeatTask implements Job {
    private static final Logger logger = LoggerFactory.getLogger(DailySeatTask.class);
    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dailySeatService;
    @DubboReference(version = "1.0.0", check = false)
    private SeatService seatService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;
    @Value("${business.daily.train.timeCount}")
    private Integer timeCount;
    @Autowired
    private IdStrUtils idStrUtils;
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("开始生成基础车座数据");
        // 查询基础车座数据
        TrainSeat trainSeat = new TrainSeat();
        List<TrainSeat> seats =  seatService.selectAllSeatByCondition(trainSeat);
        logger.info("查询到的基础车座数据:【{}】", JSON.toJSONString(seats));
        if(seats.isEmpty()){
            logger.info("每日车座数据任务结束，基础数据为空");
            return;
        }
        DateTime time = DateTime.now();

        for (Integer i = 0; i < timeCount; i++) {
            // 查询每日车厢数据，根据已有车厢分配车座
            DailyTrainCarriage dailyTrainCarriage = new DailyTrainCarriage();
            dailyTrainCarriage.setDate(time);
            List<DailyTrainCarriage> carriageList =  dailyCarriageService.selectAllCarriageByCondition(dailyTrainCarriage);
            logger.info("查询到的日期为：【{}】的车厢数据:【{}】", time, carriageList);
            if(carriageList.isEmpty()){
                continue;
            }
            List<DailyTrainSeat> insertSeats = new ArrayList<>();

            for (DailyTrainCarriage trainCarriage : carriageList) {
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
                Date now = new Date();

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
                        for(int t = 1;t < trainStations.size(); t++){
                            sell.append("0");
                        }
                        insertSeat.setSell(sell.toString());
                        insertSeat.setCreateTime(now);
                        insertSeat.setUpdateTime(now);
                        insertSeats.add(insertSeat);
                    }
                }
            }
            logger.info("需要更新的每日车座数据:【{}】", JSON.toJSONString(insertSeats));
            List<List<DailyTrainSeat>> lists = CommonUtils.splitList(insertSeats);
            for (List<DailyTrainSeat> list : lists) {
                logger.info("开始插入部分每日车座数据:【{}】", JSON.toJSONString(list));
                dailySeatService.batchInsertDSeat(list);
            }
            time = time.offset(DateField.DAY_OF_YEAR, 1);
        }
        logger.info("每日车座任务结束");
        context.getJobDetail().getJobDataMap().put("taskResult",true);

    }
}
