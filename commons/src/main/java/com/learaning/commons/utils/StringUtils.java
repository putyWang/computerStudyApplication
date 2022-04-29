package com.learaning.commons.utils;


/**
 * 字符串工具类
 */
public class StringUtils {

    private StringUtils() {}

    /**
     * 判断字符串s是否为空
     * @param cs
     * @return
     */
    public static boolean isBlank(CharSequence cs) {

        int length = length(cs);
        if (length == 0) {

            return true;
        }

        //判断字符序列是否有空格组成
        for (int i = 0; i < length; i++) {
            if (! Character.isWhitespace(cs.charAt(i))) {

                return false;
            }
        }

        return true;
    }

    public static boolean isNoneBlank(CharSequence... css) {
        return org.apache.commons.lang3.StringUtils.isNoneBlank(css);
    }

    /**
     * 获取字符序列长度
     * @param cs
     * @return
     */
    public static int length (CharSequence cs) {

        return cs == null? 0 : cs.length();
    }

    public static String camelToUnderline(String s) {
        return com.baomidou.mybatisplus.core.toolkit.StringUtils.camelToUnderline(s);
    }

    public static boolean equals(String s1, String s2) {
        return org.apache.commons.lang3.StringUtils.equals(s1, s2);
    }
}
