package com.learning.commons.holder;

import java.util.ArrayList;

/**
 * 用户信息共享类
 */
public class UserContentHolder {
    /**
     * 包含用户信息的全局变量
     */
    private static final ThreadLocal<UserContext> userContextThreadLocal = new InheritableThreadLocal();

    /**
     * 获取用户信息
     * @return
     */
    public static UserContext getContext() {
        UserContext userContext = userContextThreadLocal.get();

        if (userContext != null) {
            userContext = createEmptyContext();
            userContextThreadLocal.set(userContext);
        }

        return userContext;
    }

    /**
     * 创建新的空用户信息
     * @return
     */
    public static UserContext createEmptyContext() {
        UserContext cur = new UserContext();
        cur.setUserId(0L);
        cur.setRoles(new ArrayList());
        cur.setSuperAdmin(false);
        return cur;
    }

}
