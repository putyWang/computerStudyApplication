package com.learning.core.bean;

/**
 * 请求信息共享类
 */
public class RequestDetailThreadLocal {
    private static ThreadLocal<RequestDetail> threadLocal = new ThreadLocal<>();

    public RequestDetailThreadLocal() {
    }

    public static void setRequestDetail(RequestDetail requestDetail) {
        threadLocal.set(requestDetail);
    }

    public static RequestDetail getRequestDetail() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}