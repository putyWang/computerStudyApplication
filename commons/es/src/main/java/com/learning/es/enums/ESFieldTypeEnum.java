package com.learning.es.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public enum ESFieldTypeEnum {
    NUMBER(1, "number", "数字", "数值类型", 2),
    TEXT(2, "text", "文本", "文本类型, ES默认会分词处理", 3),
    DATE(3, "date", "日期", "日期类型,默认格式为yyyy-MM-dd", 5),
    TIME(4, "time", "时间", "时间类型,默认格式为hh:mm:ss", 6),
    DATETIME(5, "datetime", "日期时间", "日期时间类型，默认格式为yyyy-MM-dd hh:mm:ss", 4),
    KEYWORD(9, "keyword", "关键词", "关键词类型， ES不进行分词处理", 1),
    OBJECT(10, "object", "对象", "对象类型", 7);

    private int type;
    private String code;
    private String name;
    private String desc;
    private int oder;
    public static List<Map<String, Object>> typeList = new ArrayList<>();
    public static LinkedHashMap<String, ESFieldTypeEnum> enumMap = new LinkedHashMap<>();

    private ESFieldTypeEnum(int type, String code, String name, String desc, Integer oder) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.oder = oder;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public Integer getOder() {
        return this.oder;
    }

    public static ESFieldTypeEnum getByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        } else {

            for(ESFieldTypeEnum type : values()) {
                if (type.code.equals(code)) {
                    return type;
                }
            }

            return null;
        }
    }

    static {

        for(ESFieldTypeEnum type : values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type.type);
            map.put("code", type.code);
            map.put("name", type.name);
            map.put("desc", type.desc);
            map.put("order", type.oder);
            typeList.add(map);
            enumMap.put(String.valueOf(type.code), type);
        }

        typeList.sort((o1, o2) -> {
            int diff = Integer.parseInt(o1.getOrDefault("order", "0").toString()) - Integer.parseInt(o2.getOrDefault("order", "0").toString());
            if (diff > 0) {
                return 1;
            } else {
                return diff < 0 ? -1 : 0;
            }
        });
    }
}
