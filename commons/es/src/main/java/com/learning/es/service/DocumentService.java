package com.learning.es.service;

import com.learning.es.bean.ElasticCRFFillData;
import com.learning.es.constants.ElasticMethodInterface;
import com.learning.es.model.condition.ConditionBuilder;
import org.elasticsearch.index.query.QueryBuilder;

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
    void bulk(String index, List<Map<String, Object>> actions);

    /**
     * 批量从原索引迁移文档到目标索引
     * @param sourceIndex 源索引
     * @param destIndex 目标索引
     * @param bringInto 查询中必须包含的项
     * @param rulingOut 查询中肯定不包含的项
     * @param dateQuery 数据查询构造器
     */
    void bulkByQuery(
            String sourceIndex,
            String destIndex,
            ConditionBuilder bringInto,
            ConditionBuilder rulingOut,
            QueryBuilder dateQuery
    );

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
}
