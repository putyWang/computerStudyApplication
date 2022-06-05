package com.learning.web.module.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.learning.core.cache.RedisCache;
import com.learning.core.constants.RedisConstants;
import com.learning.core.utils.MD5Utils;
import com.learning.core.utils.ObjectUtils;
import com.learning.core.utils.StringUtils;
import com.learning.shiro.bean.JwtToken;
import com.learning.shiro.contants.JwtConstants;
import com.learning.shiro.utils.JwtUtils;
import com.learning.web.module.dao.UserMapper;
import com.learning.web.module.dto.LoginDto;
import com.learning.web.module.dto.UserDto;
import com.learning.web.module.entity.UserEntity;
import com.learning.web.module.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.Date;

@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, UserEntity>
        implements UserService {

    public static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public static Integer maxErrorNumber = 5;
    /**
     * redis缓存工具类
     */
    @Resource
    private RedisCache redisCache;

    /**
     * Token密钥
     */
    @Resource
    private JwtUtils jwtUtils;

    @Override
    public String login(LoginDto login) {

        String username = login.getUsername();

        String lockedKey = username + RedisConstants.USER_LOCKED_KEY;
        String passwordErrorTimesKey =  username + RedisConstants.ERROR_NUMBER_OF_PASSWORD;

        /**
         * 若账号已被锁定，直接抛出异常
         */
        if (redisCache.hasKey(lockedKey)) {
            throw new LockedAccountException("账号已被锁定，请于" + secondToTime(redisCache.getExpire(lockedKey)) + "后重新登录");
        }

        UserDto user = this.getBaseMapper().selectByUsername(username);
        Claims claims = new DefaultClaims();
        String token = "";


        //校验用户是否存在
        if (ObjectUtils.isEmpty(user)) {
            throw new UnknownAccountException("账户名有误");
        }

        //校验密码
        if (! user.getPassword().equals(MD5Utils.encrypt(login.getPassword()))) {
            Integer errorNumber = 1;
            //获取密码错误次数
            Object o = redisCache.get(passwordErrorTimesKey);
            if (o instanceof String && StringUtils.isNumeric((String)o)) {
                errorNumber += Integer.parseInt((String)o);
            }

            //如果密码错误次数大于5次 直接锁定十分钟
            if (errorNumber >= maxErrorNumber) {
                Date date = new Date();
                Date expireDate = DateUtil.offset(date, DateField.SECOND, RedisConstants.DEFAULT_USER_LOCKED_TIME_OUT.intValue());
                redisCache.set(lockedKey, expireDate.toString().substring(10), RedisConstants.DEFAULT_USER_LOCKED_TIME_OUT);
                redisCache.delete(passwordErrorTimesKey);
            } else {

                redisCache.set(passwordErrorTimesKey, errorNumber.toString(), RedisConstants.DEFAULT_USER_LOCKED_TIME_OUT);
            }

            throw new UnknownAccountException("密码有误");
        }

        //将账号相关信息加入到claims之中
        claims.put(JwtConstants.USER_NAME, username);
        claims.put(JwtConstants.ROLE_NAME, user.getRoleCode());
        claims.put(JwtConstants.PERMISSION_NAME, user.getPermissions());
        claims.put(JwtConstants.USER_STATUS, user.getStatus());

        //创建Token
        token = jwtUtils.createToken(username, user.getId(), claims);
        SecurityUtils.getSubject().login(new JwtToken(token));

        log.info(token);

        return token;
    }

    private String secondToTime (Long second) {
        StringBuilder time = new StringBuilder();

        if (second / 3600 != 0) {
            time.append(second / 3600).append( "时");
            second = second % 3600;
        }

        if (second / 60 != 0) {
            time.append(second / 60).append( "分");
            second = second % 60;
        }

        return time.append(second).append("秒").toString();
    }
}
