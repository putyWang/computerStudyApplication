package com.learning.es.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * CRF表单字段信息
 * @author ：wangpenghui
 * @date ：Created in 2021/3/18 14:35
 */
@Accessors(chain = true)
@Data
public class CRFFieldInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 关联的表单id
     */
    private Long formId;
    /**
     * 表单名称
     */
    private String formName;
    /**
     * crf表单字段名称name
     */
    private String name;
    /**
     * crf表单字段key
     */
    private String key;
    /**
     * crf表单字段type
     */
    private String type;
    /**
     * crf表单字段对应的es查询字段路径
     */
    private String path;
    /**
     * crf表单formGuid
     */
    private String formGuid;

    public CRFFieldInfo() {
    }

    public CRFFieldInfo(Long formId, String formName, String name, String key, String type, String path, String formGuid) {
        this.formId = formId;
        this.formName = formName;
        this.name = name;
        this.key = key;
        this.type = type;
        this.path = path;
        this.formGuid = formGuid;
    }

}
