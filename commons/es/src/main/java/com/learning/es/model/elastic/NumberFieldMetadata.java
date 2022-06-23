package com.learning.es.model.elastic;

/**
 * elasticsearch number类型字段类型
 *
 * @author felix
 */
public class NumberFieldMetadata extends BaseFieldMetadata {
    /**
     * 默认整型
     */
    private final static String defaultT = "integer";

    private String name;
    /**
     * 如果true，错误的数字被忽略。如果false（默认），格式错误的数字会抛出异常并拒绝整个文档。
     */
    private boolean ignoreMalformed;

    public NumberFieldMetadata(String name) {
        super(defaultT);
        this.name = name;
        this.ignoreMalformed = false;
    }

    public NumberFieldMetadata(String name, String type) {
        super(type);
        this.name = name;
        this.ignoreMalformed = false;
    }

    public String getName() {
        return name;
    }

    public boolean isIgnoreMalformed() {
        return ignoreMalformed;
    }

    public void setIgnoreMalformed(boolean ignoreMalformed) {
        this.ignoreMalformed = ignoreMalformed;
    }
}
