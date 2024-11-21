package com.train.common.utils;

import cn.hutool.core.util.StrUtil;
import com.train.common.base.entity.domain.DailyTrain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 普通工具类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/11/3 下午11:19</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
public class CommonUtils {
    public static <T> List<List<T>> splitList(List<T> list, Integer size) {
        List<List<T>> result = new ArrayList<>();

        if (list.size() > size) {
            for (int j = 0; j < list.size(); j += size) {
                int end = Math.min(j + size, list.size());
                List<T> lis2 = list.subList(j, end);
                result.add(lis2);
            }
        } else {
            result.add(list);
        }
        return result;
    }
    public static <T> List<List<T>> splitList(List<T> list) {
        return splitList(list, 200);
    }

    public static boolean canSell(Integer startIndex, Integer endIndex, String sell) {
        if(startIndex == null || endIndex == null || StrUtil.isBlank(sell)){
            return false;
        }
        for(int i = startIndex - 1; i <= endIndex - 2 ; i ++){
            if(sell.charAt(i) == '1'){
                return false;
            }
        }
        return true;
    }
    public static Date Y_M_D_H_m_STOY_M_D(Date date){
        String format = new SimpleDateFormat("yyyy-MM-dd").format(date) + " 00:00:00";
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date parse = dateTimeFormat.parse(format);
            return parse;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args){
        System.out.println(canSell(3,4, "001"));
    }
}
