package com.learning.es.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum TuoMinEnum {
    NOT_TM(0, "not_tm", "不脱敏", ""),
    NAME_TM(1, "name_tm", "姓名脱敏", ""),
    ID_CARD_TM(2, "id_card_tm", "身份证脱敏", ""),
    REGNO_TM(3, "regno_tm", "登记号脱敏", ""),
    PHONE_TM(4, "phone_tm", "手机号脱敏", ""),
    EMAIL_TM(5, "email_tm", "电子邮箱脱敏", ""),
    ADDRESS_TM(6, "address_tm", "地址脱敏", "");

    private int type;
    private String code;
    private String name;
    private String desc;
    public static List<Map<String, Object>> typeList = new ArrayList();
    public static Map<String, TuoMinEnum> enumMap = new HashMap();
    public static Map<Integer, TuoMinEnum> enumTypeMap = new HashMap();

    private TuoMinEnum(int type, String code, String name, String desc) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public int getType() {
        return this.type;
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

    static {
        TuoMinEnum[] types = values();
        TuoMinEnum[] var1 = types;
        int var2 = types.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            TuoMinEnum type = var1[var3];
            Map<String, Object> map = new HashMap();
            map.put("type", type.type);
            map.put("code", type.code);
            map.put("name", type.name);
            map.put("desc", type.desc);
            typeList.add(map);
            enumMap.put(String.valueOf(type.code), type);
            enumTypeMap.put(type.type, type);
        }

    }
}
