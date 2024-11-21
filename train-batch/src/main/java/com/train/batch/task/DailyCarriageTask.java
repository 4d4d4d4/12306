package com.train.batch.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.util.DateUtils;
import com.train.batch.annotation.TaskDesc;
import com.train.common.base.entity.domain.DailyTrain;
import com.train.common.base.entity.domain.DailyTrainCarriage;
import com.train.common.base.entity.domain.TrainCarriage;
import com.train.common.base.entity.query.DailyCarriageQuery;
import com.train.common.base.entity.query.DailyTrainCarriageExample;
import com.train.common.base.entity.query.DailyTrainQuery;
import com.train.common.base.service.CarriageService;
import com.train.common.base.service.DailyCarriageService;
import com.train.common.base.service.DailyTrainService;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日生成车厢任务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/3 下午5:46</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Component
@TaskDesc("生成三日车厢数据")
@DisallowConcurrentExecution
public class DailyCarriageTask implements Job {
    private static final Logger logger = LoggerFactory.getLogger(DailyCarriageTask.class);
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;
    @DubboReference(version = "1.0.0", check = false)
    private CarriageService carriageService;
    @Autowired
    private IdStrUtils idStrUtils;
    @Value("${business.daily.train.timeCount}")
    private Integer timeCount;
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("开启生成每日车厢任务");
        // 先查询车厢基础数据
        List<TrainCarriage> carriageList = carriageService.selectAllCarriageWithCondition(null);

        Date now = new Date();
        DateTime time = DateTime.now();
        for(int i = 0; i< timeCount ; i++) {
            // 查询每日火车数据
            DailyTrainQuery dailyTrainQuery = new DailyTrainQuery();
            dailyTrainQuery.setDate(time.toSqlDate());
            List<DailyTrain> dailyTrains = dailyTrainService.selectDTrainByCondition(dailyTrainQuery);
            if(dailyTrains.isEmpty()){
                continue;
            }
            DailyCarriageQuery dailyCarriageQuery = new DailyCarriageQuery();
            dailyCarriageQuery.setDate(dailyCarriageQuery.getDate());
            // 删除time的车厢数据
            dailyCarriageService.batchDelCarriageByCondition(dailyCarriageQuery);
            for(DailyTrain train : dailyTrains){
                // 根据每日火车数据判断哪些车厢可以添加
                List<TrainCarriage> list = carriageList.stream().filter((item) -> item.getTrainCode().equals(train.getCode())).toList();
                if(list.isEmpty()){
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
                logger.info("写入每日车厢数据:【{}】", JSON.toJSON(dailyTrainCarriages));
                dailyCarriageService.batchInsertCarriage(dailyTrainCarriages);
            }

            time = time.offset(DateField.DAY_OF_YEAR, 1);
        }
        logger.info("生成每日车厢任务结束");
    }
}
