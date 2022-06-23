package com.learning.es.model.elastic;

public class BaseFieldMetadata {
    /**
     * 字段类型， 默认text
     */
    private String type = "text";

    /**
     * 映射字段级查询时间提升。接受浮点数，默认为1.0。
     */
    private double boost = 1.0;

    private Object nullValue;

    protected BaseFieldMetadata(String type) {
        this.type = type;
        this.nullValue = null;
    }

    public String getType() {
        return type;
    }

    public double getBoost() {
        return boost;
    }

    public void setBoost(double boost) {
        this.boost = boost;
    }

    public Object getNullValue() {
        return nullValue;
    }

    public void setNullValue(Object nullValue) {
        this.nullValue = nullValue;
    }
}
