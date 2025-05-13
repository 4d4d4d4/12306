package com.train.common.base.entity.resp;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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
 * <li>Date : 2025/4/22 下午1:07</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2025，. All rights reserved.
 * @Author cqy.
 */
@Data
public class OrderConfirmResp implements Serializable {
    private Map<String, Integer> mapByCode; // 根据火车编码分类的票数，key为火车编码，integer为对应的数量
    private Map<String, Integer> mapByTime; // 根据日期分类的票数，key为日期，integer为对应的数量 日期格式为yyyy-MM-dd
    private Map<String, Integer> mapByTimeCode; // 根据日期和火车编码分类的票数，key格式为"日期_火车编码"，integer为对应的数量
}
