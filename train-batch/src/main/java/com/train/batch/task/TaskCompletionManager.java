package com.train.batch.task;
import org.quartz.JobKey;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
 * <li>Date : 2024/12/1 下午6:21</li>
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
public class TaskCompletionManager {
    private static final ConcurrentHashMap<JobKey, CompletableFuture<Void>> taskCompletionMap = new ConcurrentHashMap<>();

    public static void addTask(JobKey jobKey, CompletableFuture<Void> future) {
        taskCompletionMap.put(jobKey, future);
    }

    public static CompletableFuture<Void> getTask(JobKey jobKey) {
        return taskCompletionMap.get(jobKey);
    }

    public static void removeTask(JobKey jobKey) {
        taskCompletionMap.remove(jobKey);
    }
}
