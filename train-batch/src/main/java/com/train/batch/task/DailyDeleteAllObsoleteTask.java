package com.train.batch.task;

import com.train.batch.annotation.TaskDesc;
import com.train.common.base.service.DailyCarriageService;
import com.train.common.base.service.DailySeatService;
import com.train.common.base.service.DailyTicketService;
import com.train.common.base.service.DailyTrainService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 删除所有的过时任务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/12/2 下午3:32</li>
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
@TaskDesc(value = "删除所有的超时任务", order = 10)
@Component
public class DailyDeleteAllObsoleteTask implements Job {
    private final Logger log = LoggerFactory.getLogger(DailyDeleteAllObsoleteTask.class);
    @DubboReference(version = "1.0.0", check = false)
    private DailyTrainService dailyTrainService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyCarriageService dailyCarriageService;
    @DubboReference(version = "1.0.0", check = false)
    private DailySeatService dailySeatService;
    @DubboReference(version = "1.0.0", check = false)
    private DailyTicketService dailyTicketService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        try {
            Date date = new Date();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd");
            log.info("删除{}前旧数据", dateTimeFormat.format(date));
            int delTrain = dailyTrainService.delDTrainBeforeNow(date);
            log.info("删除旧火车数据{}条", delTrain);
            int delCarriage = dailyCarriageService.delDCarriageBeforeNow(date);
            log.info("删除旧车厢数据{}条", delCarriage);
            int delSeat = dailySeatService.delDSeatBeforeNow(date);
            log.info("删除旧车座数据{}条", delSeat);
            int delTicket = dailyTicketService.delDTicketBeforeNow(date);
            log.info("删除旧车票数据{}条", delTicket);

            log.info("删除旧数据结束");
        }catch (Exception ignored){
            log.info("删除旧数据任务失败：{}", ignored.getMessage());
        }finally {
            CompletableFuture<Void> future = TaskCompletionManager.getTask(jobKey);
            if (future != null) {
                future.complete(null); // 标记任务完成
                TaskCompletionManager.removeTask(jobKey); // 移除记录
            }
        }
    }
}
