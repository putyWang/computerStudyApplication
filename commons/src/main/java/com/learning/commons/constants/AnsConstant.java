package com.learning.commons.constants;

/**
 *  公有的静态常量类
 *  用于封装常用的数据
 */
public class AnsConstant {

    /**
     * Constant 加入 用户名 key 常量
     * 用户名在Token中的key名，为jwt-user-name-key
     */
    public static final String JWT_USER_NAME="jwt-user-name-key";

    /**
     * 角色信息Key
     */
    public static final String ROLES_INFOS_KEY="roles-infos-key";

    /**
     * 权限信息Key
     */
    public static final String POWER_KEY="power-key";

    /**
     * 业务访问Token accessToken
     */
    public static final String ACCESS_TOKEN = "authorization";

    /**
     * 主动去刷新 token key(适用场景 比如修改了用户的角色/权限去刷新token)
     */
    public static final String JWT_REFRESH_KEY="jwt-refresh-key_";

    /**
     * 标记用户是否已经被锁定
     */
    public static final String ACCOUNT_LOCK_KEY="account-lock-key_";

    /**
     * 标记用户是否已经删除
     */
    public static final String DELETED_USER_KEY="deleted-user-key_";

    /**
     * 用户权鉴缓存 key
     */
    public static final String IDENTIFY_CACHE_KEY="shirocache:com.wz.lesson.shiro.CustomRealm.authorizationCache:";
}
