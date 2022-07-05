package com.learning.es.model.elastic;


import com.learning.es.constants.ElasticConst;

import java.util.ArrayList;
import java.util.Map;

/**
 * 链接字段元数据
 */
public class JoinFieldMetadata {
    /**
     *
     */
    private final static String N = "join";

    /**
     * 字段类型
     */
    private final String type;

    /**
     * 链接字段名
     */
    private final String joinField;

    /**
     * 构建全局序号
     */
    private boolean eagerGlobalOrdinals;

    /**
     * 关系数组
     */
    private Map<String, ArrayList<String>> relations;

    public JoinFieldMetadata() {
        this.type = N;
        this.joinField = ElasticConst.ELASTIC_FIELD_JION_FIELD;
        this.eagerGlobalOrdinals = true;
    }

    public String getType() {
        return type;
    }

    public Map<String, ArrayList<String>> getRelations() {
        return relations;
    }

    public void setRelations(Map<String, ArrayList<String>> relations) {
        this.relations = relations;
    }

    public String getJoinField() {
        return joinField;
    }

    public boolean isEagerGlobalOrdinals() {
        return eagerGlobalOrdinals;
    }

    public void setEagerGlobalOrdinals(boolean eagerGlobalOrdinals) {
        this.eagerGlobalOrdinals = eagerGlobalOrdinals;
    }
}
