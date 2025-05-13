package com.train.batch.controller;

import cn.hutool.core.util.StrUtil;
import com.train.batch.annotation.TaskDesc;
import com.train.batch.entity.BaseTaskResp;
import com.train.batch.entity.CronTaskReq;
import com.train.batch.entity.CronTaskResp;
import com.train.batch.task.TaskCompletionManager;
import com.train.common.aspect.annotation.CacheEvictByPrefix;
import com.train.common.resp.Result;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.quartz.listeners.JobChainingJobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.*;

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

    private static List<String> classPathsInPackage = new ArrayList<>();

    static {
        classPathsInPackage = getClassPathsInPackage(taskPackagePath);
        log.info("初始化任务包下的所有task:{}", classPathsInPackage);
    }

    @GetMapping("/testTask")
    public Result testTask() {
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            log.info("获取到的任务包下的路径 :{}", classPathsInPackage);
            for (String s : classPathsInPackage) {
                JobDetail jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(s)).withIdentity(s, "test").build();
                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule("0/10 * * * * ?");
                CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(s, "test").withDescription("注释").withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, trigger);
            }
        } catch (Exception e) {
            log.error("测试任务失败：{}", e.getMessage());
        }
        return Result.ok();
    }

    // 获取所有已有任务信息
    @GetMapping("/getTask")
    public Result getAllTask() {
        List<BaseTaskResp> result = new ArrayList<>();
        for (String s : classPathsInPackage) {
            BaseTaskResp baseTask = new BaseTaskResp();
            Class<?> aClass = null;
            try {
                aClass = Class.forName(s);
                if (!Job.class.isAssignableFrom(aClass)) {
                    continue;
                }
                TaskDesc annotation = aClass.getAnnotation(TaskDesc.class);
                if (annotation == null) {
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
    @CacheEvictByPrefix(prefix = "train_")
    public Result runTask(@RequestBody CronTaskReq task) {
        String name = task.getName();
        String group = task.getGroup();
        CompletableFuture<Void> future = new CompletableFuture<>();
        JobKey jobKey = JobKey.jobKey(name, group);
        TaskCompletionManager.addTask(jobKey, future); // 添加任务状态到管理器
        try {
            schedulerFactoryBean.getScheduler().triggerJob(jobKey);
            CronTriggerImpl trigger = (CronTriggerImpl) schedulerFactoryBean.getScheduler().getTrigger(TriggerKey.triggerKey(name, group));
            trigger.setPreviousFireTime(new Date());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        // 工作量较大的任务分组需要合理等待再返回结果
        if(group.equals("batch")){
            try {
                future.get(85, TimeUnit.SECONDS); // 阻塞等待任务完成
            } catch (InterruptedException | ExecutionException  e) {
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                return Result.error().message("任务超时，请手动检查数据是否正确");
            }
        }
        return Result.ok();
    }
    public boolean isJobRunning(Scheduler scheduler, String jobName, String jobGroup) throws SchedulerException {
        log.info("现在执行的任务有：{}",scheduler.getCurrentlyExecutingJobs());
        for (JobExecutionContext executingJob : scheduler.getCurrentlyExecutingJobs()) {
            String executingJobName = executingJob.getJobDetail().getKey().getName();
            String executingJobGroup = executingJob.getJobDetail().getKey().getGroup();
            if (executingJobName.equals(jobName) && executingJobGroup.equals(jobGroup)) {
                return true;
            }
        }
        return false;
    }
    // 添加任务
    @PostMapping("/addTask")
    public Result addTask(@RequestBody CronTaskReq task) {
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
        } catch (Exception e) {
            e.printStackTrace();
            log.error("任务添加失败:{}", e.getMessage());
        }
        return Result.ok();
    }

    // 暂停任务
    @PostMapping("/pauseTask")
    public Result pause(@RequestBody CronTaskReq taskReq) {
        String name = taskReq.getName();
        String group = taskReq.getGroup();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();

            scheduler.pauseJob(JobKey.jobKey(name, group));
        } catch (Exception e) {
            log.error("暂停定时任务失败:" + e);
            return Result.error().message("暂停任务失败");
        }
        return Result.ok();
    }

    // 删除任务
    @PostMapping("/delTask")
    public Result deleteTask(@RequestBody CronTaskReq cronTask) {
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
    public Result resetTask(@RequestBody CronTaskReq taskReq) {
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
    public Result queryTask() {
        List<CronTaskResp> result = new ArrayList<>();
        try {
            Scheduler scheduler = schedulerFactoryBean.getScheduler();
            for (String groupName : scheduler.getJobGroupNames()) {
                // 根据所有任务组名获取具体任务列表
                for (JobKey jk : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    CronTaskResp cronTaskResp = new CronTaskResp();
                    JobDetail jobDetail = scheduler.getJobDetail(jk);
                    String name = jk.getName();
                    String group = jk.getGroup();
                    cronTaskResp.setName(name);
                    cronTaskResp.setGroup(group);
                    // 获取该任务的所有触发器
                    List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(jk);
                    for (Trigger trigger : triggersOfJob) {
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
        return Result.ok().data("result", result);
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

    @PostMapping("runAllJob")
    public Result takeAllJob() throws SchedulerException {
        ArrayList<String> temp = new ArrayList<>(classPathsInPackage);
        temp.sort((o1, o2) -> {
            try {
                Class<?> c1 = Class.forName(o1);
                Class<?> c2 = Class.forName(o2);
                TaskDesc annotation1 = c1.getAnnotation(TaskDesc.class);
                TaskDesc annotation2 = c2.getAnnotation(TaskDesc.class);
                return annotation1.order() - annotation2.order();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        // 获取 Scheduler 实例
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        // 使用 CountDownLatch 来等待所有任务完成
        CountDownLatch latch = new CountDownLatch(temp.size());

        // 注册 JobListener，确保任务串行执行
        JobListener jobListener = new JobListener() {
            @Override
            public String getName() {
                return "SerialJobListener";
            }

            @Override
            public void jobToBeExecuted(JobExecutionContext context) {
                // 任务即将执行，可以做一些预处理
            }

            @Override
            public void jobExecutionVetoed(JobExecutionContext context) {
                // 如果任务被拒绝执行
            }

            @Override
            public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
                // 当前任务执行完毕后，调度下一个任务
                latch.countDown();  // 标记任务完成
                try {
                    // 获取下一个任务的类名
                    String nextJobClass = getNextJobClass(context.getJobDetail().getKey().getName());
                    if (nextJobClass != null) {
                        // 动态加载并调度下一个任务
                        Class<?> nextJobClassObj = Class.forName(nextJobClass);
                        JobDetail nextJobDetail = JobBuilder.newJob((Class<? extends Job>) nextJobClassObj)
                                .withIdentity(nextJobClass)  // 使用类名作为 Job 的唯一标识
                                .build();
                        Trigger nextTrigger = TriggerBuilder.newTrigger()
                                .withIdentity("trigger_" + nextJobClass)
                                .startNow()  // 立即开始执行
                                .build();
                        scheduler.scheduleJob(nextJobDetail, nextTrigger);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private String getNextJobClass(String currentJobName) {
                int currentIndex = temp.indexOf(currentJobName);
                return currentIndex + 1 < temp.size() ? temp.get(currentIndex + 1) : null;  // 返回下一个任务的类名
            }
        };
        scheduler.getListenerManager().addJobListener(jobListener);
        String className = temp.get(0);
        try {
            // 动态加载任务类
            Class<?> clazz = Class.forName(className);

            // 确保该类实现了 Quartz 的 Job 接口
            if (Job.class.isAssignableFrom(clazz)) {
                // 获取 JobDetail，检查任务是否已经注册
                JobKey jobKey = new JobKey(className);
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (jobDetail == null) {
                    // 如果任务尚未注册，则创建 JobDetail
                    jobDetail = JobBuilder.newJob((Class<? extends Job>) clazz)
                            .withIdentity(className)  // 使用类名作为 Job 的唯一标识
                            .build();
                }

                // 创建 Trigger，保证任务立即执行
                Trigger trigger = TriggerBuilder.newTrigger()
                        .withIdentity("trigger_" + className)
                        .startNow()  // 立即开始执行
                        .build();

                // 调度任务
                scheduler.scheduleJob(jobDetail, trigger);
            } else {
                throw new RuntimeException("任务类 " + className + " 没有实现 Quartz 的 Job 接口");
            }
        } catch (Exception e) {
            throw new RuntimeException("执行任务时出错：" + className, e);
        }

        // 启动 Scheduler 开始任务调度
        scheduler.start();
        // 等待所有任务完成
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 所有任务完成后，移除监听器
        boolean b = scheduler.getListenerManager().removeJobListener(jobListener.getName());
        log.info("是否成功删除监听器:{}", b ? "成功" : "失败");
        return Result.ok();
    }


    // 获取包名下的所有类
    public static List<String> getClassPathsInPackage(String packageName) {
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

    public static void main(String[] args) {
//        List<String> classPathsInPackage1 = getClassPathsInPackage(taskPackagePath);
//        classPathsInPackage1.sort((o1, o2) -> {
//            try {
//                Class<?> c1 = Class.forName(o1);
//                Class<?> c2 = Class.forName(o2);
//                TaskDesc annotation1 = c1.getAnnotation(TaskDesc.class);
//                TaskDesc annotation2 = c2.getAnnotation(TaskDesc.class);
//                return annotation1.order() - annotation2.order();
//            } catch (ClassNotFoundException e) {
//                throw new RuntimeException(e);
//            }
//        });
//        System.out.println(classPathsInPackage1);
        String newSell = "11111";
        String sell = "10001";
        System.out.println("Integer.parseInt(newSell):" + Integer.parseInt(newSell,2));
        System.out.println("Integer.parseInt(sell):" + Integer.parseInt(sell,2));
        System.out.println("~Integer.parseInt(sell):" + ~Integer.parseInt(sell,2));
        System.out.println("Integer.parseInt(newSell) & (~Integer.parseInt(sell)" + (Integer.parseInt(newSell,2) & (~Integer.parseInt(sell))));
        String affectSell = Integer.toBinaryString(Integer.parseInt(newSell,2) & (~Integer.parseInt(sell,2)));
        System.out.println(affectSell);
        affectSell = StrUtil.fillBefore(affectSell,'0',sell.length());
        System.out.println(affectSell);

    }
}
