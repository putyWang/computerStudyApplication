package com.learning.core.utils;

import javax.servlet.http.HttpServletRequest;

public final class BrowserUtil {
    public static final String IE = "msie";
    public static final String FIREFOX = "firefox";
    public static final String CHROME = "chrome";

    private BrowserUtil() {
        throw new AssertionError();
    }

    /**
     * 判断浏览器类型
     * @param request
     * @return
     */
    public static String getCurrent(HttpServletRequest request) {
        String userAgent = request.getHeader("USER-AGENT").toLowerCase();
        if (userAgent != null && !"".equals(userAgent.trim())) {
            if (userAgent.contains("chrome")) {
                return "chrome";
            }

            if (userAgent.contains("firefox")) {
                return "firefox";
            }

            if (userAgent.contains("msie")) {
                return "msie";
            }
        }

        return null;
    }

    public static boolean isIe(HttpServletRequest request) {
        return "msie".equals(getCurrent(request));
    }

    public static boolean isFirefox(HttpServletRequest request) {
        return "firefox".equals(getCurrent(request));
    }

    public static boolean isChrome(HttpServletRequest request) {
        return "chrome".equals(getCurrent(request));
    }
}
