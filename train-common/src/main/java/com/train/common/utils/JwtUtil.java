package com.train.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.AeadAlgorithm;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
@ConfigurationProperties("jwt.config")
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.config.secretKey}")
    private String property;
    // 指定签名的时候使用的签名算法，也就是header那部分
    private static SecretKey key;

    @Value("${jwt.config.ttlMills}")
    private String ttlMillsStr;

    private static Long ttlMills;

    @Value("${jwt.config.jwtId}")
    private String jwtIdStr;
    private static String jwtId;

    @Value("${jwt.config.subject}")
    private String subjectStr;
    private static String subject;

    private static AeadAlgorithm enc;

    @PostConstruct
    public void init() {
        System.out.println(123);
        if (ttlMillsStr == null) {
            ttlMills = 1000 * 60 * 60 * 12L;
        } else {
            ttlMillsStr = ttlMillsStr.trim();
            String[] split = ttlMillsStr.split("\\*");
            Long ttl = 1L;
            for (String s : split) {
                long l = Long.parseLong(s);
                ttl = ttl * l;
            }
            ttlMills = ttl;
            if(ttlMills < 1000 * 60 * 60 * 12L){
                ttlMills = 1000 * 60 * 60 * 12L;
            }
        }
        if (property == null) {
            key = Jwts.SIG.HS256.key().build();
            enc = Jwts.ENC.A128CBC_HS256;
        } else {
            if (property.equalsIgnoreCase("hs256")) {
                key = Jwts.SIG.HS256.key().build();
                enc = Jwts.ENC.A128CBC_HS256;
            } else if (property.equalsIgnoreCase("hs384")) {
                key = Jwts.SIG.HS384.key().build();
                enc = Jwts.ENC.A192CBC_HS384;
            } else if (property.equalsIgnoreCase("hs512")) {
                key = Jwts.SIG.HS512.key().build();
                enc = Jwts.ENC.A256CBC_HS512;
            } else {
                key = Jwts.SIG.HS256.key().build();
                enc = Jwts.ENC.A128CBC_HS256;
            }
        }
        subject = subjectStr;
        jwtId = jwtIdStr;
        System.out.println(key.getAlgorithm() + "   " + enc + "  " + subject + " " + jwtId + "   " + ttlMills);
    }

    /**
     * 生成jwt
     * 使用Hs256算法, 私匙使用固定秘钥
     *
     * @param claims 设置的信息
     * @return
     */
    public static String createJWT(Map<String, Object> claims) {
        // 生成JWT的时间
        long expMillis = System.currentTimeMillis() + ttlMills;
        Date exp = new Date(expMillis);

        String jwt = Jwts
                .builder()
                .claims(claims) // 放置的参数
                .expiration(exp)
                .id(jwtId)
                .header()
                .and()        // JwtBuilder
                .subject(subject)
                .encryptWith(key, enc)
                .compact();

        return jwt;
    }

    /**
     * Token解密
     * jwt秘钥 此秘钥一定要保留好在服务端, 不能暴露出去, 否则sign就可以被伪造, 如果对接多个客户端建议改造成多个
     *
     * @param token 加密后的token
     * @return
     */
    public static Claims parseJWT(String token) {
        // 得到DefaultJwtParser
        Claims payload = Jwts.parser().decryptWith(key).build().parseEncryptedClaims(token).getPayload();
        boolean isSubject = payload.getSubject().equals(subject);
        if (!isSubject) {
            log.info("登录失败，:token为：{},失败原因：{}", token, "jwt的主题与期望的不符合");
            throw new JwtException("登录异常，请重新登录");
        }
        boolean isId = payload.getId().equals(jwtId);
        if (!isId) {
            log.info("登录失败，:token为：{},失败原因：{}", token, "jwt的id与期望的不符合");
            throw new JwtException("登录异常，请重新登录");
        }
        Claims claims = payload;

        return claims;
    }

    public static Long getMemberId(String token){
        Claims claims = parseJWT(token);
        return (Long) claims.get("memberId");
    }
}
