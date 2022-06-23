package com.learning.es.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * es 分词类型
 */
public enum AnalyzerEnum {
    /**
     * 不分词
     */
    NOT_ANALYZER(0, "not_analyzer", "不分词", ""),
    /**
     * ik ik_max_word分词
     */
    IK_MAX_WORD(1, "ik_max_word", "ik_max_word", ""),
    /**
     * ik ik_smart 分词
     */
    IK_SMART(2, "ik_smart", "ik_smart", ""),

    ;
    private Integer type;
    private String code;
    private String name;
    private String desc;

    AnalyzerEnum(Integer type, String code, String name, String desc) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }


    public static List<Map<String, Object>> typeList = new ArrayList<>();

    static {
        AnalyzerEnum[] types = AnalyzerEnum.values();
        for (AnalyzerEnum type : types) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type.type);
            map.put("code", type.code);
            map.put("name", type.name);
            map.put("desc", type.desc);
            typeList.add(map);
        }
    }

    public static AnalyzerEnum getByType(Integer type) {
        AnalyzerEnum[] types = AnalyzerEnum.values();
        for (AnalyzerEnum t : types) {
            if (t.type.equals(type)) {
                return t;
            }
        }

        return null;
    }
}
