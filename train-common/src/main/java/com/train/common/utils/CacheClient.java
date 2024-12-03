package com.train.common.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.train.common.base.entity.data.RedisData;
import com.train.common.entity.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

///**********************
@Component
@Slf4j
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // 设置TTL时间
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    // 设置逻辑过期
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        // 设置逻辑过期
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        //
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    // 缓存穿透
    // 定义泛型R ID   R为返回结果的类型 ID 为id的类型  参数和返回值都是泛型的函数对象Function
    public <R, ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 根据商铺id 尝试在redis中获取数据
        String json = stringRedisTemplate.opsForValue().get(key);
        // 判断shopJson是否存在 判断shopJson是一个非空字符串
        if (StrUtil.isNotBlank(json)) {
            // 1.若获取到数据返回数据
            return JSONUtil.toBean(json, type);
        }
        // 判断shopJson 是一个空字符串
        if (null != json) {
            return null;
        }

        // 2.若没有尝试在数据库中获取数据
        R result = dbFallBack.apply(id);
        if (result == null) {
            // 为了避免缓存穿透而引起数据库过大的压力 当商品信息为null的缓存一个空值对象 并设置一个短的TTL时间以节省内存空间
            // 还有一种方法叫做布隆过滤 ---> 将数据的值进行hash算法 当有数据请求时根据hash值判断是否存在（不存在是一定对的 存在是可能不存在的） TODO 有时间了解一下吧~
            stringRedisTemplate.opsForValue().set(key, "", Duration.ofMinutes(SystemConstants.CACHE_NULL_TTL));
            // 2-1.若没有获取到数据 返回404
            return null;
        }


        // 2-2.若获取到数据 将数据写入到redis中 返回数据
        // 缓存数据30+分钟 后面跟随了一个随机数以避免缓存雪崩(这里指由大量key同时失效引起的)情况的出现
        this.set(key, result, time + RandomUtil.randomInt(1, 5), unit);
        return result;
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <R, ID> List<R> queryListWithLocalExpire(String keyPrefix, ID id, Class<R> type, Function<ID, List<R>> dbFallBack, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 从redis中查询缓存信息
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            // 尝试获取互斥锁
            String lock_key = SystemConstants.CACHE_MUTEX_LOCK_KEY + id;
            boolean isLock = tryGetLock(lock_key);

            // 1.判断是否成功获取互斥锁
            List<R> rs = null;
            if (isLock) {
                // 1.1成功开启独立的线程 进行数据重构
                // 此处的try catch只能包裹在里面 因为这是个独立的线程
                try {
                    // 重建缓存
                    rs = saveDataToRedis(keyPrefix, id, time, unit, dbFallBack);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unLock(lock_key);
                }

            }
            return rs;
        }
        // 将获取到的json字符串转化为类
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
//        System.out.println(shopRedisData.getData()  +"\n 它的类是：" + shopRedisData.getData().getClass());
        // 在将RedisData中Object数据转化为Json字符串的时候 因为不清楚Object的具体类 会将他转化为JsonObject数据
        List<R> result = JSONUtil.toList(JSONUtil.toJsonPrettyStr(redisData.getData()), type);
        // 判断数据是否过期
        if (redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            // 数据未过期返回数据
            return result;
        }
        // 数据过期
        // 尝试获取互斥锁
        String lock_key = SystemConstants.CACHE_MUTEX_LOCK_KEY + id;
        boolean isLock = tryGetLock(lock_key);
        // 1.判断是否成功获取互斥锁
        if (isLock) {
            // 1.1成功开启独立的线程 进行数据重构
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                // 此处的try catch只能包裹在里面 因为这是个独立的线程
                try {
                    // 重建缓存
                    saveDataToRedis(keyPrefix, id, time, unit, dbFallBack);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unLock(lock_key);
                }
            });

        }
        // 1.2失败先返回旧的数据
        return result;


    }

    // 获取 🔒
    private boolean tryGetLock(String key) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(SystemConstants.CACHE_MUTEX_CLOCK_TTL));
        // boolean封装类Boolean在拆箱过程中可能会返回空值
        return BooleanUtil.isTrue(result);
    }

    //删除 🔒
    private boolean unLock(String key) {
        Boolean result = stringRedisTemplate.delete(key);
        return BooleanUtil.isTrue(result);
    }

    // 存具有逻辑过期时间的Shop数据
    public <ID, R> List<R> saveDataToRedis(String keyPrefix, ID id, Long sen, TimeUnit unit, Function<ID, List<R>> dbFallBack) {
        System.out.println(id + "   " + sen);
        List<R> data = dbFallBack.apply(id);
        try {
            // 模拟延迟
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        RedisData redisData = new RedisData();
        redisData.setData(data);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(sen)));
        log.info("缓存的key是:{}", keyPrefix + id);
        stringRedisTemplate.opsForValue().set(keyPrefix + id, JSONUtil.toJsonStr(redisData));
        return data;
    }

}
