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

    void bulk(String var1, List<Map<String, Object>> var2);

    void bulkByQuery(String var1, String var2, ConditionBuilder var3, ConditionBuilder var4, QueryBuilder var5);

    void bulkByCaseIds(String var1, String var2, List<String> var3);

    void bulkByCaseIds(String var1, String var2, List<String> var3, String var4, String var5);

    void bulkByCaseIds(ElasticMethodInterface var1, String var2, String var3, List<String> var4, String var5, String var6);

    void updateByDocId(String var1, String var2, String var3, String var4);

    void updateByDocId(String var1, String var2, String var3, Map var4);

    void updateCRFFillData(String var1, String var2, String var3, String var4, String var5);

    void updateCRFFillDatas(String var1, List<ElasticCRFFillData> var2);
}
