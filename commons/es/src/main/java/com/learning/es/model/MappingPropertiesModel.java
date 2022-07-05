package com.learning.es.model;

import com.learning.es.model.elastic.BaseFieldMetadata;
import com.learning.es.model.elastic.JoinFieldMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * 属性映射模型
 */
public class MappingPropertiesModel {

    /**
     * 字段元数据列表
     */
    private List<BaseFieldMetadata> metadata = new ArrayList<>();

    /**
     * 关联字段对象
     */
    private JoinFieldMetadata joinFieldMetadata = new JoinFieldMetadata();

    public MappingPropertiesModel() {
    }

    public List<BaseFieldMetadata> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(List<BaseFieldMetadata> metadata) {
        this.metadata = metadata;
    }

    public JoinFieldMetadata getJoinFieldMetadata() {
        return this.joinFieldMetadata;
    }

    public void setJoinFieldMetadata(JoinFieldMetadata joinFieldMetadata) {
        this.joinFieldMetadata = joinFieldMetadata;
    }

    public void addMetadata(BaseFieldMetadata metadata) {
        if (metadata != null) {
            this.metadata.add(metadata);
        }
    }
}
