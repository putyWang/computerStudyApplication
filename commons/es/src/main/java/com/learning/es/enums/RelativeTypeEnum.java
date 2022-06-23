package com.learning.es.enums;

import java.util.*;

/**
 * 关系词类型枚举类
 *
 * @author felix
 */
public enum RelativeTypeEnum {
    /**
     * 等于 =  eq
     */
    EQUAL(1, "eq", "等于", "=", ""),
    /**
     * 大于 > gt
     */
    GEATER_THAN(2, "gt", "大于", ">", ""),
    /**
     * 小于 < lt
     */
    LESS_THAN(3, "lt", "小于", "<", ""),

    /**
     * 不等于
     */
    NOT_EQUAL(4, "ne", "不等于", "!=", ""),

    /**
     * 大于等于
     */
    GEATER_THAN_EQUAL(5, "gte", "大于等于", ">=", ""),

    /**
     * 小于等于
     */
    LESS_THAN_EQUAL(6, "lte", "小于等于", "<=", ""),

    /**
     * 包含
     */
    INCLUDE(7, "include", "包含", "包含", ""),

    /**
     * 不包含
     */
    NOT_INCLUDE(8, "not_include", "不包含", "不包含", ""),

    IN(9, "in", "多值匹配", "包含对各值", "包含多个值中任意一个即可"),

    /**
     * 开区间查询
     */
    BTEWEENT_OPEN(10, "between_open", "开区间", "开区间", ""),

    /**
     * 闭区间查询
     */
    BTEWEENT_OFF(11, "between_off", "闭区间", "闭区间", ""),
    /**
     * 左开右闭区间
     */
    BTEWEENT_LEFT(12, "between_left", "左开右闭区间", "左开右闭区间", ""),
    /**
     * 左闭右开区间
     */
    BTEWEENT_RIGHT(13, "between_right", "左闭右开区间", "左闭右开区间", ""),
    /**
     * 字段值为空值
     */
    IS_NULL(14, "is_null", "为空", "为空", ""),
    /**
     * 字段值不为空
     */
    NOT_NULL(15, "not_null", "非空", "非空", ""),
    ;
    private int type;
    private String code;
    private String name;
    private String character;
    private String desc;

    RelativeTypeEnum(int type, String code, String name, String character, String desc) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.character = character;
        this.desc = desc;
    }

    public static List<Map> typeList = new ArrayList<>();
    public static LinkedHashMap<String, RelativeTypeEnum> enumMap = new LinkedHashMap<>();

    static {
        RelativeTypeEnum[] types = RelativeTypeEnum.values();
        for (RelativeTypeEnum type : types) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", type.type);
            map.put("code", type.code);
            map.put("name", type.name);
            map.put("desc", type.desc);
            map.put("character", type.character);
            typeList.add(map);
            enumMap.put(String.valueOf(type.code), type);
        }
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

    public String getDesc() {
        return desc;
    }

    public String getCharacter() {
        return character;
    }

    public static RelativeTypeEnum getByCode(String code) {
        for (RelativeTypeEnum type : RelativeTypeEnum.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }

        return null;
    }
}
