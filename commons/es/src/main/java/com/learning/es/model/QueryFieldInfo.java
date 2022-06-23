package com.learning.es.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 字段查询信息
 * @author wangpenghui
 */
@Data
public final class QueryFieldInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 查询逻辑
     */
    private String logical;
    /**
     * 关系词
     */
    private String relative;
    /**
     * 字段信息
     */
    private FieldItemInfo fieldItemInfo;
    /**
     * 其他字段信息
     */
    private FieldItemInfo otherFieldItemInfo;
    /**
     * 查询值
     */
    private Object value;
    /**
     * 查询最值
     */
    private Object endValue;
    /**
     * 其他值
     */
    private Object otherValue;
    /**
     * 条件中文描述
     */
    private String condition;
    /**
     * 条件比较符中文描述
     */
    private String compare;
    /**
     * 查询的名称
     */
    private String searchItemName;

}
