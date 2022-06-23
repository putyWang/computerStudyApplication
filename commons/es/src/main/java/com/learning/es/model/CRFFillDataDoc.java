package com.learning.es.model;

import lombok.Data;

/**
 * ES中CRF表单填写数据相关信息
 * @author ：wangpenghui
 * @date ：Created in 2020/10/28 17:36
 */
@Data
public class CRFFillDataDoc {
    /**
     * CRF数据id
     */
    private String docId;
    /**
     * 表单填写数据json字符串
     */
    private String fillData;
    /**
     * 登记号
     */
    private String regno;
    /**
     * 表单form_guid
     */
    private String formGuid;
    /**
     * 子项目id
     */
    private Long subProjectId;
    /**
     * 随访id
     */
    private Long visitId;

}