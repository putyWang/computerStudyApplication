package com.learning.shiro.utils;

import cn.hutool.core.date.DateUtil;
import com.learning.core.utils.CollectionUtils;
import com.learning.core.utils.StringUtils;
import com.learning.shiro.contants.JwtConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtils {


    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 此方法需要详细解读
     * 签发token
     * @param issuer    签发人
     * @param subject   代表这个JWT的主体，即它的所有人 一般是用户id
     * @param claims  有效时间(秒)
     */
    public String createToken(String issuer, Long subject, Claims claims) {

        Date nowDate = new Date();
        //设置token时间 以秒为单位
        Date expireDate = new Date(nowDate.getTime() + jwtProperties.getExpire() * 1000);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setIssuer(issuer)
                .setSubject(subject.toString())
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecret())
                .compact();
    }

    //从数据声明生成令牌
    private String createToken(Claims claims) {

        return createToken(claims.getIssuer(), Long.parseLong(claims.getSubject()), claims);
    }

    /**
     * 通过载荷名字获取载荷的值，获取token列名
     */
    public Claims getClaim(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret())
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 当原来的token没过期时是可以刷新的
     *
     * @param token 带tokenHead的token
     */
    public String refreshToken(String token) {

        if (StringUtils.isEmpty(token)) {
            return null;
        }

        if (StringUtils.isEmpty(token)) {
            return null;
        }
        //token校验不通过
        Claims claim = getClaim(token);
        if (CollectionUtils.isEmpty(claim)) {
            return null;
        }
        //如果token已经过期，不支持刷新
        if (isTokenExpired(token)) {
            return null;
        }
        //如果token在30分钟之内刚刷新过，返回原token
        if (tokenRefreshJustBefore(token, 30 * 60)) {
            return token;
        } else {
            return createToken(claim);
        }
    }

    /**
     * 判断token在指定时间内是否刚刚刷新过
     *
     * @param token 原token
     * @param time  指定时间（秒）
     */
    private boolean tokenRefreshJustBefore(String token, int time) {
        Claims claims = getClaim(token);
        // 获取创建时间
        Date created = DateUtil.offsetSecond(claims.getExpiration(), (int)jwtProperties.getExpire() * -1);

        Date refreshDate = new Date();
        //刷新时间在创建时间的指定时间内
        return refreshDate.after(created) && refreshDate.before(DateUtil.offsetSecond(created, time));
    }

    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getClaim(token).getExpiration();
        return expiration.before(new Date());
    }
}
