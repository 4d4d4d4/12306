package com.train.common.entity;
/**
 * 常用静态字段
 */
public class SystemConstants {
    // 车站缓存前缀
    public static final String CACHE_STATION_PREFIX = "train_station_";
    public static final String CACHE_STATION_NAME_List_PREFIX = "name_list::";
    // 车座缓存
    public static final String CACHE_SEAT_PREFIX = "train_seat_";
    public static final String CACHE_SEAT_QUERY_ALL_SEAT = "query_all_seat::";
    public static final long CACHE_NULL_TTL = 2L;  //缓存空值有效时间
    public static final String CACHE_MUTEX_LOCK_KEY = "mutex-lock:"; //重构缓存数据所用的互斥锁(存入redis)的前缀
    public static final long CACHE_MUTEX_CLOCK_TTL = 10; //互斥锁的持续时间

    // 售票索key前缀
    public static String TICKET_LOCK_PREFIX = "ticket_lock_";
}
