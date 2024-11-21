package com.train.batch.controller;

import com.train.batch.annotation.TaskDesc;
import com.train.batch.entity.BaseTaskResp;
import com.train.batch.entity.CronTaskReq;
import com.train.batch.entity.CronTaskResp;
import com.train.common.resp.Result;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 任务控制类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/27 下午3:26</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@RestController
@RequestMapping("/task")
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;
    private static final String taskPackagePath = "com.train.batch.task";

    @GetMapping("/testTask")
    public Result testTask(){
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            List<String> classPathsInPackage = getClassPathsInPackage(taskPackagePath);
            log.info("获取到的任务包下的路径 :{}", classPathsInPackage);
            for(String s : classPathsInPackage){
                JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(s)).withIdentity(s, "test").build();
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/10 * * * * ?");
                CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(s, "test").withDescription("注释").withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, trigger);
            }
        }catch (Exception e){
            log.error("测试任务失败：{}", e.getMessage());
        }
        return Result.ok();
    }

    // 获取所有已有任务信息
    @GetMapping("/getTask")
    public Result getAllTask(){
        List<String> classPathsInPackage = getClassPathsInPackage(taskPackagePath);
        List<BaseTaskResp> result = new ArrayList<>();
        for(String s : classPathsInPackage){
            BaseTaskResp baseTask = new BaseTaskResp();
            Class<?> aClass = null;
            try {
                aClass = Class.forName(s);
                if(!Job.class.isAssignableFrom(aClass)){
                    continue;
                }
                TaskDesc annotation = aClass.getAnnotation(TaskDesc.class);
                if(annotation == null){
                    continue;
                }
                String group = annotation.group();
                String value = annotation.value();
                baseTask.setDesc(value);
                baseTask.setGroup(group);
                baseTask.setName(s);
                result.add(baseTask);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return Result.ok().data("result", result);
    }

    // 启动任务
    @PostMapping("/runTask")
    public Result runTask(@RequestBody CronTaskReq task){
        String name = task.getName();
        String group = task.getGroup();
        try {
            schedulerFactoryBean.getScheduler().triggerJob(JobKey.jobKey(name, group));
            CronTriggerImpl trigger =(CronTriggerImpl) schedulerFactoryBean.getScheduler().getTrigger(TriggerKey.triggerKey(name, group));
            trigger.setPreviousFireTime(new Date());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return Result.ok();
    }

    // 添加任务
    @PostMapping("/addTask")
    public Result addTask(@RequestBody CronTaskReq task){
        String desc = task.getDesc();
        String name = task.getName();
        String cronExpression = task.getCronExpression();
        String group = task.getGroup();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            scheduler.start();
            // 创建任务
            JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(name)).withIdentity(name, group).build();
            // 创建定时调度构造器
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            // 构造触发器
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, group).withDescription(desc).withSchedule(cronScheduleBuilder).forJob(jobDetail).build();
            scheduler.scheduleJob(jobDetail, trigger);
        }catch (Exception e){
            e.printStackTrace();
            log.error("任务添加失败:{}", e.getMessage());
        }
        return Result.ok();
    }

    // 暂停任务
    @PostMapping("/pauseTask")
    public Result pause(@RequestBody CronTaskReq taskReq){
        String name = taskReq.getName();
        String group = taskReq.getGroup();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            scheduler.pauseJob(JobKey.jobKey(name, group));
        }catch (Exception e){
            log.error("暂停定时任务失败:" + e);
            return Result.error().message("暂停任务失败");
        }
        return Result.ok();
    }

    // 删除任务
    @PostMapping("/delTask")
    public Result deleteTask(@RequestBody CronTaskReq cronTask){
        String name = cronTask.getName();
        String group = cronTask.getGroup();
        String desc = cronTask.getDesc();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            // 暂停对应的触发器
            scheduler.pauseTrigger(TriggerKey.triggerKey(name, group));
            // 删除触发器
            scheduler.unscheduleJob(TriggerKey.triggerKey(name, group));
            // 删除任务
            scheduler.deleteJob(JobKey.jobKey(name, group));
        } catch (SchedulerException e) {
            log.error("任务删除失败");
            return Result.error().message(desc + "的任务删除失败");
        }
        return Result.ok();
    }


    // 重置任务
    @PostMapping("/resetTask")
    public Result resetTask(@RequestBody CronTaskReq taskReq){
        String name = taskReq.getName();
        String group = taskReq.getGroup();
        String cronExpression = taskReq.getCronExpression();
        String desc = taskReq.getDesc();

        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            // 要重置的触发器key
            TriggerKey triggerKey = TriggerKey.triggerKey(name, group);
            // 构建新的运行触发器
            CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
            CronTriggerImpl trigger = (CronTriggerImpl) scheduler.getTrigger(triggerKey);
            // 重置任务开始时间
            trigger.setStartTime(new Date());
            CronTrigger trigger1 = trigger;
            CronTrigger build = trigger1.getTriggerBuilder().withSchedule(cronScheduleBuilder).withDescription(desc).withIdentity(triggerKey).build();
            scheduler.rescheduleJob(triggerKey, build);
        } catch (SchedulerException e) {
            log.error("{}任务重置失败", name);
           return Result.error().message(desc + "的任务重置失败");
        }
        return Result.ok();
    }

    // 查询任务
    @GetMapping("/queryTask")
    public Result queryTask(){
        List<CronTaskResp> result = new ArrayList<>();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            for(String groupName : scheduler.getJobGroupNames()){
                // 根据所有任务组名获取具体任务列表
                for(JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))){
                    CronTaskResp cronTaskResp = new CronTaskResp();
                    JobDetail jobDetail = scheduler.getJobDetail(jk);
                    String name = jk.getName();
                    String group = jk.getGroup();
                    cronTaskResp.setName(name);
                    cronTaskResp.setGroup(group);
                    // 获取该任务的所有触发器
                    List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jk);
                    for(Trigger trigger : triggersOfJob){
                        CronTaskResp cronTaskResp1 = new CronTaskResp();
                        BeanUtils.copyProperties(cronTaskResp, cronTaskResp1);
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        Date previousFireTime = cronTrigger.getPreviousFireTime();
                        Date nextFireTime = cronTrigger.getNextFireTime();
                        String cronExpression = cronTrigger.getCronExpression();
                        String description = cronTrigger.getDescription();

                        cronTaskResp1.setPreFireTime(previousFireTime);
                        cronTaskResp1.setDesc(description);
                        cronTaskResp1.setNextFireTime(nextFireTime);
                        cronTaskResp1.setCronExpression(cronExpression);

                        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                        cronTaskResp1.setState(triggerState.equals(Trigger.TriggerState.NORMAL));

                        result.add(cronTaskResp1);
                    }
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
            log.error("任务查询失败");
            throw new RuntimeException(e);
        }
        return  Result.ok().data("result", result);
    }

    // 重启任务
    @RequestMapping(value = "/resume")
    public Result resume(@RequestBody CronTaskReq cronJobReq) {
        String jobClassName = cronJobReq.getName();
        String jobGroupName = cronJobReq.getGroup();
        try {
            Scheduler sched = schedulerFactoryBean.getScheduler();
            sched.resumeJob(JobKey.jobKey(jobClassName, jobGroupName));
        } catch (SchedulerException e) {
           return Result.error().message("重启定时任务失败");
        }
        return Result.ok();
    }

    // 获取包名下的所有类
    public List<String> getClassPathsInPackage(String packageName) {
        List<String> classPaths = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:" + path + "/**/*.class");

            for (Resource resource : resources) {
                String uri = resource.getURI().toString();
                String classPath = uri.substring(uri.indexOf(path), uri.length() - 6); // 去掉 ".class"
                classPath = classPath.replace('/', '.');
                classPaths.add(classPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classPaths;
    }
}
