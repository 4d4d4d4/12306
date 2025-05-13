package com.train.business.aspect;

import com.train.common.base.entity.domain.Train;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.sql.Date;

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
 * <li>Date : 2025/5/5 上午11:07</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2025，. All rights reserved.
 * @Author cqy.
 */
@Aspect
@Order(0)
@Component
public class NowTimeAspect {

    public NowTimeAspect(){
        System.out.println("时间切面器");
    }
    private final static Logger log = LoggerFactory.getLogger(NowTimeAspect.class);
    @Pointcut("execution(* com.train.business.mapper.TrainMapper.insert(..))")
    public void cut(){
    }
    @Before("cut()")
    public void doAround(JoinPoint pjp) throws Throwable {
        System.out.println("拦截数据，注入参数数据");
        Object[] args = pjp.getArgs();
        Object arg = args[0]; // 获取第一个参数对象
        if( !(arg instanceof Train)){
            log.error("参数类型错误");
            return;
        }
        LocalDate localTime = LocalDate.now();
        Date date = Date.valueOf(localTime);
        Class<?> aClass = arg.getClass();
        Method setUpdateTime = aClass.getDeclaredMethod("setUpdateTime", java.util.Date.class);
        Method setCreateTIme = aClass.getDeclaredMethod("setCreateTime",  java.util.Date.class);
        setUpdateTime.invoke(arg, date);
        setCreateTIme.invoke(arg, date);
        log.info("更新的数据为 {}", Arrays.toString(pjp.getArgs()));
    }

    }


