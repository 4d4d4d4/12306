package com.train.batch.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.train.batch.annotation.TaskDesc;
import com.train.common.base.entity.domain.DailyTrainStation;
import com.train.common.base.entity.domain.TrainStation;
import com.train.common.base.service.DailyTrainService;
import com.train.common.base.service.DailyTrainStationService;
import com.train.common.base.service.TrainStationService;
import com.train.common.utils.CommonUtils;
import com.train.common.utils.IdStrUtils;
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
 * <dd>本类用于 : 生成每日火车车站任务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/5 下午3:10</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@TaskDesc("生成每日火车车站数据任务")
@DisallowConcurrentExecution
@Component
public class DailyTrainStationTask implements Job {
    private static final Logger logger = LoggerFactory.getLogger(DailyTrainStationTask.class);
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainStationService dailyTrainStationService;
    @DubboReference(version = "1.0.0", check = false)
    private TrainStationService trainStationService;
    @Autowired
    private IdStrUtils idStrUtils;
    @Value("${business.daily.train.timeCount}")
    private Integer timeCount;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("生成火车车站数据任务开始");
        // 获取所有车站基础数据
        List<TrainStation> trainStationList =  trainStationService.selectAllTStationByCondition(null);
        if(trainStationList.isEmpty()){
            logger.info("生成火车车站任务结束，基础数据为空");
            return;
        }
        DateTime time = DateTime.now();
        Date now = new Date();
        List<DailyTrainStation> insertList = new ArrayList<>();
        for (int i = 0; i < timeCount; i++) {
            // 根据日期删除数据
            DailyTrainStation trainStation = new DailyTrainStation();
            trainStation.setDate(time);
            dailyTrainStationService.batchDelTStationByCondition(trainStation);
            Date clone = (Date) time.clone();
            // 开始添加数据
            trainStationList.forEach((item) ->{
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
        logger.info("开始写入每日火车车站数据:【{}】", JSON.toJSONString(insertList));
        List<List<DailyTrainStation>> lists = CommonUtils.splitList(insertList);
        lists.forEach((item) ->{
            dailyTrainStationService.batchInsertDTStation(item);
        });

        logger.info("每日火车车站任务结束");
    }

}
