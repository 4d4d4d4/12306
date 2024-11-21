package com.train.batch.entity;

import lombok.Data;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 定时任务请求类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/28 上午12:08</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Data
public class CronTaskReq {
    private String name; // 任务名称
    private String desc;  // 任务注释
    private String group; // 任务组
    private String cronExpression; // cron表达式
}
