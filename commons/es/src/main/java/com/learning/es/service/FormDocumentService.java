package com.learning.es.service;

import java.util.List;
import java.util.Map;

/**
 * @author ：wangpenghui
 * @date ：Created in 2021/3/10 11:16
 */
public interface FormDocumentService {

    /**
     * 批量同步数据至es
     * @param index es索引
     * @param docList es文档数据
     */
    void addBatch(String index, List<Map<String, Object>> docList);

    /**
     * 删除指定id文档
     * @param index
     * @param id
     * @param routing
     * @return
     */
    boolean deleteById(String index, String id, String routing);

    /**
     * 批量删除指定id文档
     * @param index
     * @param ids
     * @return
     */
    long deleteByIds(String index, List<String> ids);

    /**
     * 批量删除指定empi文档
     * @param index
     * @param empis
     */
    long deleteByEmpis(String index, List<String> empis);
}
