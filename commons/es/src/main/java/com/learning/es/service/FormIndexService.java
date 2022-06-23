package com.learning.es.service;

import com.boot.form.query.model.ElasticSetting;

import java.io.IOException;

/**
 * @author ：wangpenghui
 * @date ：Created in 2021/3/10 11:16
 */
public interface FormIndexService {

    /**
     * 获取es索引名称
     *
     * @param subProjectId
     * @return
     */
    String getIndexName(Long subProjectId);

    /**
     * 根据index名解析获取subprojectId
     * @param index
     * @return
     */
    Long getSubProjectIdByIndex(String index);

    /**
     * 创建es索引
     * @param index es索引名称
     * @param elasticSetting setting配置
     * @return
     */
    boolean addIndex(String index, ElasticSetting elasticSetting) throws IOException;

    /**
     * 添加mapping字段
     * @param index es索引
     * @param fieldName 字段名称
     * @param fieldType 字段类型
     * @return
     */
    boolean addField(String index, String fieldName, String fieldType);

    /**
     * 判断索引是否存在
     *
     * @param indices
     * @return
     */
    boolean indexExists(String... indices);

    /**
     * 删除es索引
     * @param indices es索引名称
     * @return
     */
    boolean deleteIndex(String... indices);
}
