package com.learning.commons.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 线程共享缓存类
 */
public class TLMap {

    /**
     * ConcurrentHashMap的初始容量
     */
    private static final Integer INITIAL_CAPACITY = 4;

    /**
     * 创建线程安全的存储ConcurrentHashMap
     */
    private static final ThreadLocal<ConcurrentHashMap<String, Object>> tlMap = ThreadLocal.withInitial(
            () -> new ConcurrentHashMap<>(INITIAL_CAPACITY)
    );

    /**
     * 获取当前ConcurrentHashMap对象
     * @return
     */
    public static ConcurrentHashMap<String, Object> queryTLMap() {
        return tlMap.get();
    }

    /**
     * 获取key对应的value值
     * @param key
     * @return
     */
    public static Object getTlMap(String key) {
        return tlMap.get().get(key);
    }

    /**
     * 删除key-value键值对
     * @param key
     */
    public static void deleteTlMap(String key) {
        tlMap.get().remove(key);
    }

    /**
     * 向ConcurrentHashMap中添加元素
     * @param key
     * @param val
     */
    public static void setTlMap(String key, Object val) {
        ConcurrentHashMap<String, Object> map = tlMap.get();
        if (null == val) {
            map.put(key, "");
        } else {
            map.put(key, val);
        }
        tlMap.set(map);
    }

    /**
     * 获取user_id
     * @return
     */
    public static String getUserId() {
        if (StringUtils.isBlank(getTlMap("user_id") + "")) {
            return null;
        }
        return (String) getTlMap("user_id");
    }

    public static void setUserId(String userId) {
        ConcurrentHashMap<String, Object> map = tlMap.get();
        if (StringUtils.isBlank(userId)) {
            map.remove("user_id");
        } else {
            map.put("user_id", userId);
        }
        tlMap.set(map);
    }

    public static String getMobile() {
        if (StringUtils.isBlank(tlMap.get().get("mobile") + "")) {
            return null;
        }
        return (String) tlMap.get().get("mobile");
    }

    public static void setMobile(String mobile) {
        ConcurrentHashMap<String, Object> map = tlMap.get();
        if (StringUtils.isBlank(mobile)) {
            map.remove("mobile");
        } else {
            map.put("mobile", mobile);
        }
        tlMap.set(map);
    }

    public static String getToken() {
        if (StringUtils.isBlank(tlMap.get().get("token") + "")) {
            return null;
        }
        return (String) tlMap.get().get("token");
    }

    public static void setToken(String token) {
        ConcurrentHashMap<String, Object> map = tlMap.get();
        if (StringUtils.isBlank(token)) {
            map.remove("token");
        } else {
            map.put("token", token);
        }
        tlMap.set(map);
    }

    public static boolean getAuthUser() {
        if (null == tlMap.get().get("isAuthUser")) {
            return false;
        }
        return (boolean) tlMap.get().get("isAuthUser");
    }

    public static void setAuthUser(boolean isAuthUser) {
        ConcurrentHashMap<String, Object> map = tlMap.get();
        map.put("isAuthUser", isAuthUser);
        tlMap.set(map);
    }

    public static void destroy() {
        tlMap.remove();
    }
}