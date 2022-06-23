package com.learning.es.model.elastic;

import java.util.List;

/**
 * Elasticsarch object 类型元数据
 *
 * @author wph
 */
public class ObjectFieldMetadata extends BaseFieldMetadata {
    private final static String N = "object";
    /**
     * 属性名称
     */
    private String name;

    private List<BaseFieldMetadata> child;

    public ObjectFieldMetadata(String name) {
        super(N);
        this.name = name;
    }

    public String getName() {
        return name;
    }


}
