package com.train.common.base.service;

import com.train.common.base.entity.domain.DailyTrain;

import java.util.ArrayList;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 基础服务</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/3 下午5:12</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public class BaseService {
    public <T> List<List<T>> spiltDataList(List<T> insertList, int size){
        List<List<T>> result = new ArrayList<>();
        if(insertList.size() <= size){
            result.add(insertList);
            return result;
        }
        for(int j = 0; j < insertList.size() ; j += size){
            int end = Math.min(j + size, insertList.size());
            List<T> t = insertList.subList(j, end);
            result.add(t);
        }
        return result;
    }
    public <T> List<List<T>> spiltDataList(List<T> insertList){
        return spiltDataList(insertList, 200);
    }
}
