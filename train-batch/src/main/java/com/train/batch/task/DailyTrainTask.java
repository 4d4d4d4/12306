package com.train.batch.task;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.train.batch.annotation.TaskDesc;
import com.train.common.base.entity.domain.DailyTrain;
import com.train.common.base.entity.domain.Train;
import com.train.common.base.entity.query.DailyTrainQuery;
import com.train.common.base.service.DailyTrainService;
import com.train.common.base.service.TrainService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 每日火车相关任务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/3 下午2:54</li>
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
@TaskDesc("每日自动化生成火车任务")
@Component
public class DailyTrainTask implements Job {
    private static final Logger log = LoggerFactory.getLogger(DailyTrainTask.class);
    @DubboReference(version = "1.0.0", check = false, timeout = -1)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false, timeout = -1)
    private TrainService trainService;
    @Value("${business.daily.train.timeCount}")
    private Integer timeCount; // 每次生成的数据量/天
    @Autowired
    private IdStrUtils idStrUtils;

    /**
     * 生成3日每日火车数据
     *
     * @param context
     * @throws JobExecutionException
     */
    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("开始生成每日火车数据");
        // 查询基本数据
        List<Train> trains = trainService.selectTrainsByCode("");
        // 根据基本数据更新每日数据
        if (trains.isEmpty()) {
            log.info("每日火车任务结束， 无火车基础数据");
            return;
        }
        DateTime time = DateTime.now();
        for (int i = 0; i < timeCount; i++) {
            DailyTrainQuery dailyTrainQuery = new DailyTrainQuery();
            dailyTrainQuery.setDate(time);
//            List<DailyTrain> dTrains =  dailyTrainService.selectDTrainByCondition(dailyTrainQuery);
            // 删除重复数据
            dailyTrainService.delDTrainByCondition(dailyTrainQuery);
            DateTime finalTime = time;
            Date now = new Date();
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
            if(insertList.size() > 200){
                for(int j = 0; j < insertList.size() ; j += 200){
                    int end = Math.min(j + 200, insertList.size());
                    List<DailyTrain> dailyTrains = insertList.subList(j, end);
                    dailyTrainService.batchInsertDTrainData(dailyTrains);
                }
            }else{
                dailyTrainService.batchInsertDTrainData(insertList);
            }
            time = time.offset(DateField.DAY_OF_YEAR, 1); // 生成下一天数据
        }
        log.info("生成每日火车数据任务结束");

    }

    public static void main(String[] args) {
        DateTime now = DateTime.now();
        for (int i = 0; i < 90; i++) {
            System.out.println(StringTool.timeToStringByFormat(now, "yyyy-MM-dd"));
            now = now.offset(DateField.DAY_OF_YEAR, 1);
        }
    }
}
