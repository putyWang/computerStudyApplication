package com.learning.shiro.contants;

/**
 * jwt常用常量
 */
public class JwtConstants {

    /**
     * 请求头所带token名
     */
    public static final String ACESS_TOKEN = "authorization";

    /**
     * claim名称
     */
    public static final String CLAIM_NAME = "claim";

    /**
     * 权限列表key
     */
    public static final String PERMISSION_NAME = "permission";

    /**
     * 用户名称key
     */
    public static final String USER_NAME = "user";

    /**
     * 角色名称key
     */
    public static final String ROLE_NAME = "role";

    /**
     * 用户状态Key
     * 1表示正常使用
     * 0表示永久封禁
     */
    public static final String USER_STATUS = "status";
}
