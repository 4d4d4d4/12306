package com.train.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @Classname GatewayApplication
 * @Description 网关模块
 * @Date 2024/7/12 下午4:30
 * @Created by 憧憬
 */
@SpringBootApplication
@ComponentScan("com.train.gateway")
public class GatewayApplication {
    private static final Logger logger = LoggerFactory.getLogger(GatewayApplication.class);
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        String startPort = environment.getProperty("server.port");
        logger.info("网关服务启动：{}:{}", "http://localhost", startPort);
    }
}
