package com.learning.es.enums;

import java.util.*;

public enum QueryTypeEnum {

    TERM(1,"term", "精确匹配"),
    RANGE(2, "range", "范围搜索"),
    TYPE(3, "type", ""),
    DOCTYPE(4, "type", ""),
    ;

    private int type;
    private String code;
    private String desc;

    QueryTypeEnum(int type, String code, String desc) {
        this.type = type;
        this.code = code;
        this.desc = desc;
    }

    public static List<Map<String, Object>> typeList = new ArrayList<>();
    public static LinkedHashMap<String, QueryTypeEnum> enumMap = new LinkedHashMap<>();

    static {
        QueryTypeEnum[] types = QueryTypeEnum.values();

        for (QueryTypeEnum type : types) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type.type);
            map.put("code", type.code);
            map.put("name", type.desc);
            typeList.add(map);
            enumMap.put(String.valueOf(type.code), type);
        }
    }

    public static QueryTypeEnum getByCode(String code) {
        for (QueryTypeEnum type : QueryTypeEnum.values()) {
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

    public String getDesc() {
        return desc;
    }
}
