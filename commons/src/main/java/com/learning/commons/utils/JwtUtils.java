package com.learning.commons.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

/**
 * JWT工具类
 */
public class JwtUtils {

    /**
     * 创建JwtToken    * @param username
     * @param password
     * @param secret 设置HMAC256加密密钥
     * @param second 设置Token过期时间
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String createToken(String username, String password, String secret, Integer second)
            throws UnsupportedEncodingException {
        //设置token时间 三小时
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.HOUR, second);
        Date date = nowTime.getTime();


        //密文生成
        return JWT.create()
                .withClaim("username", username)
                .withClaim("password", password)
                .withExpiresAt(date)
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证token的有效性
     */
    public static boolean verify(String token, String secret) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        verifier.verify(token);
        return true;
    }

    /**
     * 通过载荷名字获取载荷的值，获取token列名
     */
    public static String getClaim(String token, String name) {
        String claim = null;
        try {
            claim = JWT.decode(token).getClaim(name).asString();
        } catch (Exception e) {
            return "getClaimFalse";
        }
        return claim;
    }
}
