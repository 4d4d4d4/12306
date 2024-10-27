//package com.train.common.config;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.AeadAlgorithm;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.SpringApplication;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.Map;
//
///**
// * <dl>
// * <dt><b>类功能概述</b></dt>
// * <dd>本类用于 : </dd>
// * </dl>
// * <dl>
// * <dt><b>版本历史</b></dt>
// * <dd>
// * <ul>
// * <li>Version : </li>
// * <li>Date : 2024/10/9 上午12:16</li>
// * <li>Author : 16867</li>
// * <li>History : </li>
// * </ul>
// * </dd>
// * </dl>
// *
// * @Copyright Copyright &copy; 2024，. All rights reserved.
// * @Author cqy.
// */
//@Configuration
//public class JwtConfig {
//    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);
//
//    @Value("${jwt.config.secretKey}")
//    private String property;
//
//    @Value("${jwt.config.ttlMills}")
//    private String ttlMillsStr;
//
//    @Value("${jwt.config.jwtId}")
//    private String jwtIdStr;
//
//    @Value("${jwt.config.subject}")
//    private String subjectStr;
//    @Bean
//    public JwtUtil jwtUtil() {
//        return new JwtUtil(property, ttlMillsStr, jwtIdStr, subjectStr);
//    }
//}
