package com.learning.es.model.elastic;

import java.util.List;

/**
 * ES字段名称及类型,用于添加es mapper字段
 * @author ：wangpenghui
 * @date ：Created in 2020/10/28 17:36
 */
public class ElasticFieldType {

    /**
     * ES字段名称
     */
    private String fieldName;

    /**
     * ES字段类型
     */
    private String fieldType;

    /**
     * ES嵌套字段
     */
    private List<ElasticFieldType> child;

    public ElasticFieldType(String fieldName, String fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public List<ElasticFieldType> getChild() {
        return child;
    }

    public void setChild(List<ElasticFieldType> child) {
        this.child = child;
    }
}
