package com.learning.es.service;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Map;

/**
 * 表单es查询 service
 * @author ：wangpenghui
 * @date ：Created in 2021/3/10 11:16
 */
public interface FormQueryService {

    Map<String, Object> search(QueryBuilder queryBuilder, int page, int size, String... indices);

    List<Map<String, Object>> search(QueryParam queryParam, int page, int size, String... indices);

    List<Map<String, Object>> search(QueryBuilder queryBuilder, QueryParam queryParam, int page, int size, String... indices);

    Map<String, Object> collapseSearch(QueryBuilder queryBuilder, int page, int size, String... indices);

    List<String> searchForEmpi(QueryJson queryJson, String... indices);

    List<String> searchForEmpi(QueryBuilder queryBuilder, String... indices);

    List<Map<String, Object>> scrollSearch(List<String> empis, QueryParam queryParam, String... indices);

    QueryBuilder getRootQueryBuilder(QueryJson queryJson);
}
