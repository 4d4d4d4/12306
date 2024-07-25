package com.train.common.utils;

import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Classname RedisUtils
 * @Description redis工具类
 * @Date 2024/3/3 16:26
 * @Created by 憧憬
 */
@Component
public class RedisUtils<V> {
    @Resource
    private RedisTemplate<String,V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    public V get(String key){
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, V value){
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e){
            logger.error("设置redisKey:{},value:{}失败\n,原因：{}", key , value, e.getMessage());
            return false;
        }
    }

    // 保存时间单位为秒
    public boolean setEx(String key, V value, long time){
        try {
            if(time > 0){
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }else {
                redisTemplate.opsForValue().set(key, value);
            }
        }catch (Exception e){
            logger.error("设置redisKey:{},value:{}失败,原因：{}", key, value, e.getMessage());
            throw new BusinessException(ResultStatusEnum.CODE_500);
        }
        return true;
    }

    public boolean remove(String s) {
        try {
            if(s.isEmpty()){
                return false;
            }
           return redisTemplate.delete(s);
        }catch (Exception e){
            logger.error("邮箱删除失败，原因：{}",e.getMessage());
            return false;
        }
    }

    // 获取key对应的ttl
    public long getTTL(String key) {
        Long duration = redisTemplate.getExpire(key);
        if (duration != null) {
            return duration; // 返回以秒为单位的TTL
        } else {
            return -1; // 如果key不存在或者没有设置过期时间，返回-1
        }
    }
}
