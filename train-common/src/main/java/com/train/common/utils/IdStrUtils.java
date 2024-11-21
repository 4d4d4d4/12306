package com.train.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Classname IdUtils
 * @Description id生成工具包
 * @Date 2024/7/18 上午10:43
 * @Created by 憧憬
 */
@Component
public class IdStrUtils {

    @Value("${current.utils.workerId}")
    private Long workerId;

    @Value("${current.utils.datacenterId}") // 不能注入静态字段
    private Long datacenterId;

    private Snowflake snowflake;
    @PostConstruct
    public void init(){
        snowflake = IdUtil.getSnowflake(workerId, datacenterId);
    }

    public String snowFlakeStr() {
        return snowflake.nextIdStr();

    }

    public Long snowFlakeLong() {
        return snowflake.nextId();
    }
    public static String checkIntegerCode(){
        return RandomUtil.randomNumbers(5);
    }

}
