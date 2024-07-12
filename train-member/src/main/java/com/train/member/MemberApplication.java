package com.train.member;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * @Classname MemberApplication
 * @Description 会员模块~
 * @Date 2024/7/12 下午2:38
 * @Created by 憧憬
 */
@SpringBootApplication
@ComponentScan("com.train")
public class MemberApplication {
    private static final Logger logger = LoggerFactory.getLogger(MemberApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(MemberApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        String startPort = environment.getProperty("server.port");
        String contextPath = environment.getProperty("server.servlet.context-path");
        logger.info("会员服务启动：{}:{}{}", "http://localhost", startPort, contextPath);

    }
}
