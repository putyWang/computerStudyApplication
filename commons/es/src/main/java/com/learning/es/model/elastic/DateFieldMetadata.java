package com.learning.es.model.elastic;


public class DateFieldMetadata extends BaseFieldMetadata {
    private final static String N = "date";
    private final static String F = "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||yyyy-MM-dd HH:mm||epoch_millis";

    private String name;

    /**
     * 日期格式化类型
     */
    private String format;

    /**
     * 如果true，错误的数字被忽略。如果false（默认），格式错误的数字会抛出异常并拒绝整个文档。
     */
    private boolean ignoreMalformed;

    public DateFieldMetadata(String name) {
        super(N);
        this.name = name;
        this.format = F;
        this.ignoreMalformed = true;
        setNullValue("");
    }

    public String getName() {
        return name;
    }


    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }


    public boolean isIgnoreMalformed() {
        return ignoreMalformed;
    }

    public void setIgnoreMalformed(boolean ignoreMalformed) {
        this.ignoreMalformed = ignoreMalformed;
    }
}

