package com.train.common.utils;

import cn.hutool.core.date.DateTime;
import com.train.common.enums.SeatColumnEnum;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 字符串工具类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/17 下午9:01</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */

public class StringTool {
    private static final Map<String, Integer> letterToNumberMap = new HashMap<>();
    private static final Map<Integer, String> numberToLetterMap = new HashMap<>();
    // 根据SeatColumnEnum枚举将数字转化为对应的座位编码 key为(座位类型_数字) value为(作为编码)
    private static final Map<String, String> numberToStringMapByEnum = new HashMap<>();
    // 根据SeatColumnEnum枚举将座位编码转化为对应的座位序号 key为(座位类型_座位编码) value为(座位序号)
    private static final Map<String, Integer> stringToNumberMapByEnum = new HashMap<>();
    static{
        for (int i = 0; i < 26; i++) {
            char letter = (char) ('A' + i);
            letterToNumberMap.put(String.valueOf(letter), i + 1);
            numberToLetterMap.put(i + 1, String.valueOf(letter));
        }
        for (SeatColumnEnum value : SeatColumnEnum.values()) {
            String key1 = value.getType() + "_" + value.getNumber();
            String key2 = value.getType() + "_" + value.getCode();
            numberToStringMapByEnum.put(key1, value.getCode());
            stringToNumberMapByEnum.put(key2, value.getNumber());
        }
    }
    public static String concat(String field){
        return "%" + field + "%";
    }
    /**
     * 将字母转换为数字
     * @param letter 输入的字母
     * @return 对应的数字
     * @throws IllegalArgumentException 如果输入的字母无效
     */
    public static int letterToNumber(String letter) {
        Integer number = letterToNumberMap.get(letter);
        if (number == null) {
            throw new IllegalArgumentException("无效的字母: " + letter);
        }
        return number;
    }

    /**
     * 将数字转换为字母
     * @param number 输入的数字
     * @return 对应的字母
     * @throws IllegalArgumentException 如果输入的数字无效
     */
    public static String numberToLetter(int number) {
        String letter = numberToLetterMap.get(number);
        if (letter == null) {
            throw new IllegalArgumentException("无效的数字: " + number);
        }
        return letter;
    }

    /**
     * 将类型及座位序号转化为对应的座位类型编码
     * @param type
     * @param number
     * @return 可能返回null
     */
    public static String numberToStringByEnum( String type,int number){
        return numberToStringMapByEnum.get(type + "_" + number);
    }

    public static Integer stringToNumberByEnum(String type, String code){
        return stringToNumberMapByEnum.get(type + "_" + code);
    }

    public static String timeToStringByFormat(DateTime date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }
    public static String timeToStringByFormat(DateTime date, String pattern){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }
    public static void main(String[] args){
        String i= numberToLetter(1);
        String t = numberToLetter(26);
        System.out.println(i);
        System.out.println(t);

    }

    public static String front(String field) {
       return field + "%";
    }

    public static String formatRow(Integer row) {
        if (row == null) {
            throw new IllegalArgumentException("Row cannot be null");
        }
        // 格式化为两位数字，补零
        return String.format("%02d", row);
    }
    public static String formatRow(String row) {
        if (row == null || row.isEmpty()) {
            throw new IllegalArgumentException("Row cannot be null or empty");
        }
        try {
            // 将字符串解析为整数，然后格式化为两位数
            int numericRow = Integer.parseInt(row);
            return String.format("%02d", numericRow);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Row must be a numeric string", e);
        }
    }
}
