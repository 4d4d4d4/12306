package com.train.common.tool.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @Classname IdUtils
 * @Description id生成工具包
 * @Date 2024/7/18 上午10:43
 * @Created by 憧憬
 */
@Component
@Getter
public class IdStrUtils {

    @Value("${current.utils.workerId}")
    private Long workerId;

    @Value("${current.utils.datacenterId}") // 不能注入静态字段
    private Long datacenterId;
    private static Snowflake snowflake;

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
    public static String checkCodeUUId(){
        return UUID.randomUUID().toString().replace("-","").substring(0,4);
    }

}
