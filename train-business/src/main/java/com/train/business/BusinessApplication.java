package com.train.business;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.ArrayList;
import java.util.List;


/**
 * @Classname MemberApplication
 * @Description 业务模块~
 * @Date 2024/7/12 下午2:38
 * @Created by 憧憬
 */
@SpringBootApplication
@ComponentScan("com.train")
@MapperScan("com.train.business.mapper")
@EnableDubbo
@EnableDiscoveryClient // 开启nacos服务发现
public class BusinessApplication {
    private static final Logger logger = LoggerFactory.getLogger(BusinessApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(BusinessApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        String startPort = environment.getProperty("server.port");
        String contextPath = environment.getProperty("server.servlet.context-path");
        logger.info("会员服务启动：{}:{}{}", "http://localhost", startPort, contextPath);

        initFlowRules();
        logger.info("初始化规则.。。");
    }

    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("ticket/order/confirm");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 20.
        rule.setCount(1);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }
}
