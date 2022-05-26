package com.learning.commons.enums;

public enum Enabled implements EnumInterface<Integer>{

    YES(1),
    NO(0);

    private Integer value;

    private Enabled(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return this.value;
    }

    public Boolean exist(Integer value) {
        return this.getValue().equals(value);
    }
}
