package com.train.member.utils;

import lombok.Data;

import java.io.Serializable;

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
 * <li>Date : 2025/4/22 下午8:20</li>
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
public class WeatherResult implements Serializable {
    private String date;
    private String province;
    private String dayweather;
    private String daywind;
    private String week;
    private String daypower;
    private String daytemp;
    private String nightwind;
    private String nighttemp;
    private String dayTempFloat;
    private String nightTempFloat;
    private String nightWeather;
    private String nightPower;
}
