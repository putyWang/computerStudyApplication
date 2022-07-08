package com.learning.es.service;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚合查询
 */
public interface AggregationService {

    /**
     *
     * @param index
     * @param secondQueryBuilder
     * @return
     * @throws Exception
     */
    Map<String, Object> docTypeCountAggr(String index, QueryBuilder secondQueryBuilder) throws Exception;

    Map<String, Object> aggrByRegNos(List<String> var1);

    Map<String, Object> aggrTop(String var1, QueryBuilder var2, String var3, String var4, Integer var5, Boolean var6);

    Map<String, Object> aggrAdmCount(String var1, String var2, String var3, String var4, String var5);

    Map<String, Object> aggrAdmPatientCount(String var1, String var2, String var3);

    Map<String, Object> aggrSex(String var1, QueryBuilder var2, String var3, String var4);

    LinkedHashMap<String, Object> aggrAge(String var1, QueryBuilder var2, String var3, String var4);

    List<Map<String, Object>> aggrSexAndAdmAge(String var1);

    List<Map<String, Object>> aggrPatientAdmCount(String var1);

    LinkedHashMap<String, Object> aggrAdmDate(String var1, QueryBuilder var2, String var3, String var4);

    LinkedHashMap<String, Object> aggrAdmDateHistogram(String var1, QueryBuilder var2, String var3, String var4, String var5);

    String aggrTimeMax(String var1, QueryBuilder var2, String var3);

    String aggrTimeMin(String var1, QueryBuilder var2, String var3);
}
