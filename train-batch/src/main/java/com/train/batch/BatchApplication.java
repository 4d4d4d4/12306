package com.train.batch;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * <dl>
 * <dt><b>类功能概述</b></dt>
 * <dd>本类用于 : 跑批启动类</dd>
 * </dl>
 * <dl>
 * <dt><b>版本历史</b></dt>
 * <dd>
 * <ul>
 * <li>Version : </li>
 * <li>Date : 2024/10/27 下午3:10</li>
 * <li>Author : 16867</li>
 * <li>History : </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @Copyright Copyright &copy; 2024，. All rights reserved.
 * @Author cqy.
 */
@SpringBootApplication
@ComponentScan("com.train")
@MapperScan("com.train.batch.mapper")
@EnableDubbo
public class BatchApplication {
    private static final Logger logger = LoggerFactory.getLogger(BatchApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(BatchApplication.class, args);
        ConfigurableEnvironment environment = run.getEnvironment();
        String startPort = environment.getProperty("server.port");
        String contextPath = environment.getProperty("server.servlet.context-path");
        logger.info("会员服务启动：{}:{}{}", "http://localhost", startPort, contextPath);

    }
}
