package com.learning.es.model.elastic;

/**
 * Elasticsarch Keyword 类型元数据
 *
 * @author felix
 */
public class KeywordFieldMetadata extends BaseFieldMetadata {
    private final static String N = "keyword";
    /**
     * 属性名称
     */
    private String name;

    public KeywordFieldMetadata(String name) {
        super(N);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
