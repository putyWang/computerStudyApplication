package com.learning.commons.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public enum CodeInfoEnum {
    LOCK(1L, 1L, "LOCK_TYPE", "LOCK"),
    UNLOCK(1L, 2L, "LOCK_TYPE", "LOCK");

    public Long classId;
    public Long infoId;
    public String classCode;
    public String infoCode;

    private CodeInfoEnum(Long classId, Long infoId, String classCode, String infoCode) {
        this.classId = classId;
        this.infoId = infoId;
        this.classCode = classCode;
        this.infoCode = infoCode;
    }

    public static CodeInfoEnum getByInfoId(Long infoId) {
        return valueOf(infoId + "");
    }

    public static List<CodeInfoEnum> getByClassId(Long classId) {
        return Arrays.stream(values()).filter((item) -> {
            return item.classId.equals(classId);
        }).collect(Collectors.toList());
    }

    public static CodeInfoEnum getByClassCodeAndInfoCode(String classCode, String infoCode) {
        Optional<CodeInfoEnum> opt = Arrays.stream(values()).filter((item) -> {
            return item.classCode.equals(classCode) && item.infoCode.equals(infoCode);
        }).findFirst();
        return (CodeInfoEnum)opt.orElse(null);
    }

    public String toString() {
        return "CodeInfoEnum{classId=" + this.classId + ", infoId=" + this.infoId + ", classCode='" + this.classCode + '\'' + ", infoCode='" + this.infoCode + '\'' + '}';
    }
}
