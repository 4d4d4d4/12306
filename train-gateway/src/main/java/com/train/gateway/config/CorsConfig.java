//package com.train.gateway.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
//import org.springframework.web.cors.reactive.CorsWebFilter;
//
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
///**
// * @Classname CrosConfig
// * @Description 什么也没有写哦~
// * @Date 2024/3/29 9:11
// * @Created by 憧憬
// */
//@Configuration
//public class CorsConfig {
//    public CorsConfiguration corsConfiguration(){
//        CorsConfiguration configuration = new CorsConfiguration();
//        List<String> origins = Arrays.asList("http://localhost:6000/**", "http://localhost:6001/**","http://localhost:8081/**");
//        configuration.setAllowedOrigins(origins);
//        configuration.setAllowCredentials(true);
//        configuration.setAllowedHeaders(Collections.singletonList("*"));
//        configuration.setAllowedMethods(Collections.singletonList("*"));
//        return configuration;
//    }
//
//    @Bean
//    public CorsWebFilter corsWebFilter(){
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfiguration());
//        return new CorsWebFilter(source);
//    }
//}
