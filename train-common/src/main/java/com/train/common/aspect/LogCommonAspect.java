package com.train.common.aspect;

import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.train.common.aspect.annotation.GlobalAnnotation;
import com.train.common.resp.Result;
import com.train.common.resp.enmus.ResultStatusEnum;
import com.train.common.resp.exception.BusinessException;
import com.train.common.utils.JwtUtil;
import com.train.common.utils.ThreadLocalUtils;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @Classname LogCommonAspect
 * @Description 日志公用模块
 * @Date 2024/7/12 下午3:49
 * @Created by 憧憬
 */
@Aspect
@Component
@Order(1) // 优先级较高
public class LogCommonAspect {
    private Logger logger = LoggerFactory.getLogger(LoggerFactory.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public LogCommonAspect() {
        logger.info("加载全局拦截器....");
    }

    // 日志aop切入点 train包下的所有controller的所有方法
    @Pointcut("execution(public * *..*Controller.*(..))")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        // 生成日志流水
        MDC.put("LOG_ID", System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 3));

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = null;
        try {
            request = Objects.requireNonNull(requestAttributes).getRequest();
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
            logger.info("请求地址：{},  请求类型:{}", requestURL, method);
            logger.info("访客地址:{}", remoteAddr);
        } catch (Exception e) {
            logger.error("日志解析失败{}", e.getMessage());
        }


        Signature signature = pjp.getSignature();
        String name = signature.getName();
        logger.info("请求方法名:{}", name);

        // 打印方法参数
        Object[] args = pjp.getArgs();
        List<Object> arguments = new ArrayList<>();
        for (Object arg : args) {
            if (arg instanceof ServletRequest ||
                    arg instanceof ServletResponse ||
                    arg instanceof MultipartFile) {
                continue;
            }
            arguments.add(arg);
        }
        // 过滤敏感信息
        String[] excludeProperties = {"reference"};
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter argumentPropertyPreFilter = filters.addFilter(excludeProperties);

        // 设置 JSON 序列化选项
        if (arguments.isEmpty()) {
            try {
                String jsonString = objectMapper.writeValueAsString(arguments); // 格式化输出
                logger.info("接收到的参数：{}", jsonString);
            } catch (Exception e) {
                logger.error("参数解析错误:{}", arguments);
            }
        }
        logger.info("接收到的参数:{}", "");
        GlobalAnnotation annotation = ((MethodSignature) pjp.getSignature()).getMethod().getAnnotation(GlobalAnnotation.class); // 判断方法是否有全局注解


        if (annotation != null && annotation.checkLogin()) {
            // 需要校验获取token字符串解析数据
            String token = Objects.requireNonNull(request).getHeader("Authorization");
            try {
                Long memberId = JwtUtil.getMemberId(token);
                if (memberId == null) {
                    throw new BusinessException(ResultStatusEnum.CODE_504);
                }
                ThreadLocalUtils.setCurrentId(memberId);
                logger.info("当前会员id：{}", ThreadLocalUtils.getCurrentId());
            } catch (Exception e) {
                throw new BusinessException(ResultStatusEnum.CODE_504);
            }
        }
        Object proceed = pjp.proceed();

        String result = objectMapper.writeValueAsString(proceed); // 格式化输出
        if (proceed instanceof ResponseEntity<?>) {
            ResponseEntity<Result> response = (ResponseEntity<Result>) proceed;
            System.out.println(response);
            return response;
        }
        logger.info("返回的数据是：{}", result);
        logger.info("程序运行时间为:{}ms", (System.currentTimeMillis() - startTime));

        return proceed;
    }
}
