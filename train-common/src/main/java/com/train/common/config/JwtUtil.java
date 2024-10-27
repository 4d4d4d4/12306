//package com.train.common.config;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.JwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.AeadAlgorithm;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.Map;
//
//public class JwtUtil {
//    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
//
//    private static SecretKey key;
//    private static Long ttlMills;
//    private static String jwtId;
//    private static String subject;
//    private static AeadAlgorithm enc;
//
//    public JwtUtil(String property, String ttlMillsStr, String jwtIdStr, String subjectStr) {
//        synchronized (JwtUtil.class) {
//            initialize(property, ttlMillsStr, jwtIdStr, subjectStr);
//        }
//    }
//
//    private void initialize(String property, String ttlMillsStr, String jwtIdStr, String subjectStr) {
//        if (ttlMillsStr == null) {
//            ttlMills = 1000 * 60 * 60 * 12L; // 默认值
//        } else {
//            ttlMillsStr = ttlMillsStr.trim();
//            String[] split = ttlMillsStr.split("\\*");
//            Long ttl = 1L;
//            for (String s : split) {
//                long l = Long.parseLong(s);
//                ttl *= l;
//            }
//            ttlMills = ttl;
//            if (ttlMills < 1000 * 60 * 60 * 12L) {
//                ttlMills = 1000 * 60 * 60 * 12L; // 最小值
//            }
//        }
//
//        if (property == null) {
//            key = Jwts.SIG.HS256.key().build();
//            enc = Jwts.ENC.A128CBC_HS256;
//        } else {
//            switch (property.toLowerCase()) {
//                case "hs256":
//                    key = Jwts.SIG.HS256.key().build();
//                    enc = Jwts.ENC.A128CBC_HS256;
//                    break;
//                case "hs384":
//                    key = Jwts.SIG.HS384.key().build();
//                    enc = Jwts.ENC.A192CBC_HS384;
//                    break;
//                case "hs512":
//                    key = Jwts.SIG.HS512.key().build();
//                    enc = Jwts.ENC.A256CBC_HS512;
//                    break;
//                default:
//                    key = Jwts.SIG.HS256.key().build();
//                    enc = Jwts.ENC.A128CBC_HS256;
//                    break;
//            }
//        }
//
//        subject = subjectStr;
//        jwtId = jwtIdStr;
//
//        log.info("JWT:  key： {}, enc: {}, subject: {}, jwtId: {}, ttlMills: {}",
//                key, enc, subject, jwtId, ttlMills);
//    }
//
//    public String createJWT(Map<String, Object> claims) {
//        long expMillis = System.currentTimeMillis() + ttlMills;
//        Date exp = new Date(expMillis);
//
//        String jwt = Jwts
//                .builder()
//                .claims(claims)
//                .expiration(exp)
//                .id(jwtId)
//                .subject(subject)
//                .encryptWith(key, enc)
//                .compact();
//
//        return jwt;
//    }
//
//    public Claims parseJWT(String token) {
//        log.info("当前的key是:{}", key);
//        Claims payload = Jwts.parser().enc().and().decryptWith(key).build().parseEncryptedClaims(token).getPayload();
//
//        if (!payload.getSubject().equals(subject)) {
//            log.info("登录失败，token为：{},失败原因：{}", token, "jwt的主题与期望的不符合");
//            throw new JwtException("登录异常，请重新登录");
//        }
//        if (!payload.getId().equals(jwtId)) {
//            log.info("登录失败，token为：{},失败原因：{}", token, "jwt的id与期望的不符合");
//            throw new JwtException("登录异常，请重新登录");
//        }
//
//        return payload;
//    }
//
//    public Long getMemberId(String token) {
//        Claims claims = this.parseJWT(token);
//        return (Long) claims.get("memberId");
//    }
//}
