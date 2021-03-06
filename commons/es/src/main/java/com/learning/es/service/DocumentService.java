package com.learning.es.service;

import java.util.List;
import java.util.Map;

/**
 * 文档操作服务类
 */
public interface DocumentService {

    /**
     * 构造批量查插入请求
     * @param index 索引
     * @param actions 批量对应的操作数组
     */
    void bulkInsert(String index, List<Map<String, Object>> actions);

    /**
     * 根据jsonString与文档id更新文档
     * @param index 索引
     * @param docId 文档id
     * @param routing 分片
     * @param jsonString 更新对象的json字符串
     */
    void updateByDocId(String index, String docId, String routing, String jsonString);

    /**
     * 根据dataMap与文档id更新文档
     * @param index 索引
     * @param docId 文档id
     * @param routing 分片
     * @param data 更新对象的dataMap
     */
    void updateByDocId(String index, String docId, String routing, Map<String, Object> data);

    /**
     * 删除指定id文档
     * @param index 索引
     * @param docIds 文档id数组
     */
    void deleteByDocIds(String index, List<String> docIds);
}
