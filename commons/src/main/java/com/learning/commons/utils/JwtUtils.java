package com.learning.commons.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtils {

    /**
     * # JWT 密钥
     * # 业务短时间刷新Token过期时间
     * # PC端长时间刷新Token自动刷新时间
     * # APP端长时间刷新Token自动刷新时间
     * # 发行人
     * 我们也可以不将以上数据写在配置文件中，可以直接写在此类中作为此类的成员静态常量，或者独立一个公共静态常量类，另外一种jwt写法详见工程Q:\JWT_Login_About\jwt_01
     */
    private static String secretKey;
    private static Duration accessTokenExpireTime;
    private static Duration refreshTokenExpireTime;
    private static Duration refreshTokenExpireAppTime;
    private static String issuer;

    // 初始化JWT的方法 // 取得JWT配置类中取得的配置文件中的默认值
    public static void setJwtProperties(TokenSetting tokenSetting) {
        secretKey = tokenSetting.getSecretKey();
        accessTokenExpireTime = tokenSetting.getAccessTokenExpireTime();
        refreshTokenExpireTime = tokenSetting.getRefreshTokenExpireTime();
        refreshTokenExpireAppTime = tokenSetting.getRefreshTokenExpireAppTime();
        issuer = tokenSetting.getIssuer();
    }

    /**
     * 此方法需要详细解读
     * 签发token
     * @param issuer    签发人
     * @param subject   代表这个JWT的主体，即它的所有人 一般是用户id
     * @param claims    存储在JWT里面的信息 一般放些用户的权限/角色信息
     * @param second 有效时间(秒)
     */
    public static String createToken(String issuer, String subject, Map<String, Object> claims, String secret, Integer second)
            throws UnsupportedEncodingException {
        //设置token时间 以秒为单位
        Calendar nowTime = Calendar.getInstance();
        nowTime.add(Calendar.SECOND, second);
        Date date = nowTime.getTime();

        //密文生成
        return JWT.create()
                .withSubject(subject)
                .withIssuer(issuer)
                .withExpiresAt(date)
                .withClaim("user", claims)
                .withIssuedAt(new Date())
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 生成短时间刷新access_token
     * @param subject 用户ID
     * @param claims 载荷部分参数，即此用户的个人信息
     * @return
     */
    public static String getAccessToken(String subject, Map<String, Object> claims) {
        return createToken(issuer, subject, claims, accessTokenExpireTime.toMillis(), secretKey);
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
