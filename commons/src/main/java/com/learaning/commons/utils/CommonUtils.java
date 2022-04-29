package com.learaning.commons.utils;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.learaning.commons.bean.KeyValue;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public abstract class CommonUtils {
    private static String SLAT = "354816d26912441ab280f08831c38453";
    public static final AntPathMatcher antPathMatcher = new AntPathMatcher();
    public static Pattern pattern = Pattern.compile("\\$\\{(\\w+)}");

    private CommonUtils() {
    }

    public static boolean matchPath(List<String> list, String path) {
        if (CollectionUtils.isEmpty(list)) {
            return false;
        } else if (!list.contains(path)) {
            Optional<String> any = list.stream().filter((s) -> {
                return antPathMatcher.match(s, path);
            }).findAny();
            return any.isPresent();
        } else {
            return true;
        }
    }

    public static String camelToUnderline(String source) {
        return StringUtils.camelToUnderline(source);
    }

    public static String underlineToCamel(String source) {
        return StringUtils.underlineToCamel(source);
    }

    public static String getPassKey(long id) {
        String tokenTmp = DigestUtils.md5DigestAsHex((SLAT + "_" + id).getBytes(Charset.forName("UTF-8")));
        return tokenTmp;
    }

    public static String UUID() {
        return UUID.randomUUID().toString().replaceAll("\\-", "");
    }

    public static String formatDate(Date date) {
        return formatDate(date, "yyyy-MM-dd");
    }

    public static String formatDate(Date date, String pattern) {
        return (new SimpleDateFormat(pattern)).format(date);
    }

    public static String toJSONString(Object object) {
        return JSONObject.toJSONString(object);
    }

    public static List<KeyValue> formatKeyValue(Map<? extends Object, String> map) {
        return map.entrySet().stream().map((entry) -> {
            return new KeyValue(entry.getKey(), entry.getValue());
        }).collect(Collectors.toList());
    }

    public static Map<String, String> formatterKeyValueMap(List<KeyValue> keyValues) {
        return keyValues.stream().filter((keyValue) -> {
            return keyValue.getId() != null && keyValue.getText() != null;
        }).collect(Collectors.toMap((keyValue) -> {
            return String.valueOf(keyValue.getId());
        }, KeyValue::getText, (v1, v2) -> {
            return v2;
        }));
    }

    public static String replaceFormatString(String source, Map<String, Object> map) {
        Matcher matcher = pattern.matcher(source);
        StringBuffer sb = new StringBuffer();

        while(matcher.find()) {
            String variable = matcher.group(1);
            Object value = map.get(variable);
            if (value != null) {
                matcher.appendReplacement(sb, String.valueOf(value));
            }
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap();
        map.put("one", 1);
        map.put("two", 2);
        String key = "hello ${one} ${two} ${three}";
        String s = replaceFormatString(key, (Map)map);
        System.out.println(s);
        String demo = "/**/swgg.html/**";
        System.out.println(antPathMatcher.match(demo, "/swgg.html"));
    }

    public static String replaceFormatString(String source, Object param) {
        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(param));
        return replaceFormatString(source, jsonObject);
    }
}

