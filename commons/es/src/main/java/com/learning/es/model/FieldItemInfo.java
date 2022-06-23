package com.learning.es.model;


import lombok.Data;

import java.io.Serializable;

/**
 * 字段详细信息
 * @author wangpenghui
 */
@Data
public final class FieldItemInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 关联的字段id
     */
    private int fieldId;
    /**
     * 关联的indexID
     */
    private int indexId;
    /**
     * 关联的typeID
     */
    private int typeId;
    /**
     * 在ES中的字段名称
     */
    private String fieldName;
    /**
     * 字段编码
     */
    private String fieldCode;
    /**
     * 映射的indexName
     */
    private String indexName;
    /**
     * 映射的typeName
     */
    private String typeName;
    /**
     * 关联的crf表字段查询信息
     * "crf":{														// crf map对象
     * 		"formId": 17,											// crf表单id
     * 		"name": "crf基本信息表性别",							// crf表单字段名称name
     * 		"code": "input_6",										// crf表单字段key
     * 		"type": "input",										// crf表单字段type
     * 		"formGuid": "ff409872-d5cb-4984-8ad3-4f2b2b222f65"      // crf表单formGuid
     *       }
     */
    private CRFFieldInfo crf;

}
