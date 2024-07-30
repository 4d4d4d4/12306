package com.train.common.aspect;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
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
@Order(1) // 优先级较高
public class LogCommonAspect {
    public LogCommonAspect() {
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

        logger.info("请求地址：{},  请求类型:{}", requestURL, method);

        Signature signature = pjp.getSignature();
        String name = signature.getName();
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
            if (arg != null) {
                arguments[argIndex++] = arg;
            }
        }
        // 过滤敏感信息
        String[] excludeProperties = {"reference"};
        PropertyPreFilters filters = new PropertyPreFilters();
        PropertyPreFilters.MySimplePropertyPreFilter argumentPropertyPreFilter = filters.addFilter(excludeProperties);

        // 设置 JSON 序列化选项
        String jsonString = JSONObject.toJSONString(arguments, argumentPropertyPreFilter,
                SerializerFeature.DisableCircularReferenceDetect, // 禁用循环引用检测
                SerializerFeature.PrettyFormat); // 格式化输出
        logger.info("接收到的参数：{}", jsonString);

        GlobalAnnotation annotation = pjp.getClass().getAnnotation(GlobalAnnotation.class);

        if(annotation != null && annotation.checkLogin()){
            // 需要校验获取token字符串解析数据
            String token = request.getHeader("Authorization");
            Long memberId = JwtUtil.getMemberId(token);
            if(memberId == null){
                throw new BusinessException(ResultStatusEnum.CODE_504);
            }
            ThreadLocalUtils.setCurrentId(memberId);
        }

        Object proceed = pjp.proceed();

        String result = JSONObject.toJSONString(proceed, argumentPropertyPreFilter,
                SerializerFeature.DisableCircularReferenceDetect, // 禁用循环引用检测
                SerializerFeature.PrettyFormat); // 格式化输出
        if(proceed instanceof ResponseEntity<?>){
            ResponseEntity<Result> response = (ResponseEntity<Result>) proceed;
            System.out.println(response);
            return response;
        }
        logger.info("返回的数据是：{}", result);
        logger.info("程序运行时间为:{}ms", (System.currentTimeMillis() - startTime));

        return proceed;
    }
}
