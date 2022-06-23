package com.learning.es.model.elastic;


import com.learning.es.constants.ElasticConst;

import java.util.ArrayList;
import java.util.Map;

/**
 * 字段元数据
 */
public class JoinFieldMetadata {
    private final static String N = "join";
    private String type;

    private String joinField;

    /**
     * 构建全局序号
     */
    private boolean eagerGlobalOrdinals;


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
