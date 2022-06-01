package com.learning.shiro.utils;

import com.learning.shiro.contants.JwtConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@ConfigurationProperties(prefix = "jwt")
@Component
public class JwtUtils {

    private Logger log = LoggerFactory.getLogger(getClass());

    private String secret = "f4e2e52034348f86b67cde581c0f9eb5";
    private long expire = 604800L;
    private String header;

    /**
     * 此方法需要详细解读
     * 签发token
     * @param issuer    签发人
     * @param subject   代表这个JWT的主体，即它的所有人 一般是用户id
     * @param claims  有效时间(秒)
     */
    public String createToken(String issuer, String subject, Map<String, Object> claims)
            throws UnsupportedEncodingException {

        Date nowDate = new Date();
        //设置token时间 以秒为单位
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setIssuer(issuer)
                .setSubject(subject)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成短时间刷新access_token
     * @param subject 用户ID
     * @param claims 载荷部分参数，即此用户的个人信息
     * @return
     */
//    public static String getAccessToken(String subject, Map<String, Object> claims) {
//        return createToken(issuer, subject, claims, accessTokenExpireTime.toMillis(), secretKey);
//    }

    /**
     * 验证token的有效性
     */
//    public static boolean verify(String token, String secret) {
//        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
//        verifier.verify(token);
//        return true;
//    }

    /**
     * 通过载荷名字获取载荷的值，获取token列名
     */
    public Map<String, Object> getClaim(String token) {
        Map<String, Object> claim = new HashMap<>();
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("validate is token error ", e);
            return null;
        }
    }

    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getHeader() {
        return JwtConstants.ACESS_TOKEN;
    }

    public void setHeader(String header) {
        this.header = header;
    }
}
