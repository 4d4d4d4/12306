package com.train.common.aspect;

import com.train.common.aspect.annotation.CacheEvictByPrefix;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

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
 * <li>Date : 2024/11/29 下午7:21</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@Aspect
@Component
@Order(2) // 优先级较高
public class CacheEvictAspect {
    private final Logger log = LoggerFactory.getLogger(CacheEvictAspect.class);
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(cacheEvictByPrefix)")  // 拦截带有 @CacheEvictByPrefix 注解的方法
    public Object evictCacheByPrefix(ProceedingJoinPoint joinPoint, CacheEvictByPrefix cacheEvictByPrefix) throws Throwable {
        // 获取前缀并删除缓存
        String prefix = cacheEvictByPrefix.prefix();
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // 执行目标方法
        return joinPoint.proceed();
}
}
