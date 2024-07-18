package com.train.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * @Classname LogCommonAspect
 * @Description 日志公用模块
 * @Date 2024/7/12 下午3:49
 * @Created by 憧憬
 */
@Aspect
@Component
public class LogCommonAspect {
    public LogCommonAspect() {
        System.out.println("加载日志切面类");
    }

    private Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    // 日志aop切入点 train包下的所有controller的所有方法
    @Pointcut("execution(public * com.train..*Controller.*(..))")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        // 生成日志流水
        MDC.put("LOG_ID", System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 3));

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String method = request.getMethod();
        StringBuffer requestURL = request.getRequestURL();
        String remoteAddr = request.getHeader("x-forwarded-for");
        if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("Proxy-Client-IP");
        }
        if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getHeader("WL-Proxy-Client-IP");
        }
        if (remoteAddr == null || remoteAddr.isEmpty() || "unknown".equalsIgnoreCase(remoteAddr)) {
            remoteAddr = request.getRemoteAddr();
        }

        logger.info("请求地址：{}{}", requestURL, method);

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String name = signature.getMethod().getName();
        logger.info("请求方法名:{}", name);
        logger.info("访客地址:{}", remoteAddr);

        // 打印方法参数
        Object[] args = pjp.getArgs();
        Object[] arguments = new Object[args.length];
        int argIndex = 0;
        for (Object arg : args) {
            if (arg instanceof ServletRequest ||
                    arg instanceof ServletResponse ||
                    arg instanceof MultipartFile) {
                continue;
            }
            arguments[argIndex++] = arg;
        }
        // 过滤敏感信息
        String[] excludeProperties = {};
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter argumentPropertyPreFilter = filters.addFilter(excludeProperties);
//        logger.info("接收到的参数是：{}", JSONObject.toJSONString(arguments, argumentPropertyPreFilter));

        Object proceed = pjp.proceed();

//        logger.info("返回的数据是：{}", JSONObject.toJSONString(proceed, argumentPropertyPreFilter));
        logger.info("程序运行时间为:{}ms", (System.currentTimeMillis() - startTime));
        return proceed;
    }
}
