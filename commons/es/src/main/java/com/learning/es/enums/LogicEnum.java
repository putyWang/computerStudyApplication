package com.learning.es.enums;

import java.util.*;

public enum LogicEnum {

    /**
     * 和
     */
    AND(1,"and","和"),
    /**
     * 和
     */
    OR(2,"or","或"),
    /**
     * 和
     */
    NOT(3,"not","不"),
    ;

    private int type;
    private String code;
    private String name;

    LogicEnum(int type, String code, String name) {
        this.type = type;
        this.code = code;
        this.name = name;
    }

    public static List<Map<String, Object>> typeList = new ArrayList<>();
    public static LinkedHashMap<String, LogicEnum> enumMap = new LinkedHashMap<>();

    static {
        LogicEnum[] types = LogicEnum.values();

        for (LogicEnum type : types) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type.type);
            map.put("code", type.code);
            map.put("name", type.name);
            typeList.add(map);
            enumMap.put(String.valueOf(type.code), type);
        }
    }

    public static LogicEnum getByCode(String code) {
        for (LogicEnum type : LogicEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }

        return null;
    }

    public int getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
