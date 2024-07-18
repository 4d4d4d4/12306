package com.train.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * @Classname IdUtils
 * @Description id生成工具包
 * @Date 2024/7/18 上午10:43
 * @Created by 憧憬
 */
public class IdSnowFlakeUtils {

    private static final Snowflake snowflake;

    static {
        snowflake = IdUtil.getSnowflake(1, 1);
    }

    public static String snowFlakeStr() {
        return snowflake.nextIdStr();
    }

    public static Long snowFlakeLong() {
        return snowflake.nextId();
    }
}
