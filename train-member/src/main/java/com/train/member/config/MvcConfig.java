package com.train.member.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Classname MvcConfig
 * @Description 什么也没有写哦~
 * @Date 2024/7/12 下午5:21
 * @Created by 憧憬
 */
//@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedHeaders("*")
                .allowedMethods("GET","POST","PUT","DELETE","OPTION")
                .allowedOrigins("http://127.0.0.1:6000")
                .allowCredentials(true);
    }
}
