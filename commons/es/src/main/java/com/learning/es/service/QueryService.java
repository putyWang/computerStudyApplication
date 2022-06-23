package com.learning.es.service;

import com.learning.es.bean.SearchResult;
import com.learning.es.constants.ElasticMethodInterface;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.List;
import java.util.Map;

/**
 * 搜索服务类
 */
public interface QueryService {
    long count(QueryBuilder var1, String... var2);

    SearchResult search(QueryBuilder var1, int var2, int var3, String... var4);

    SearchResult search(QueryBuilder var1, SortBuilder var2, int var3, int var4, String... var5);

    SearchResult search(QueryBuilder var1, String[] var2, int var3, int var4, String... var5);

    SearchResult search(SearchSourceBuilder var1, String... var2);

    SearchResult search(SearchRequest var1);

    SearchHits searchForHits(SearchSourceBuilder var1, String... var2);

    SearchHits searchForHits(SearchSourceBuilder var1, HighlightBuilder var2, int var3, int var4, String... var5);

    SearchHits searchForHits(SearchRequest var1);

    SearchHits searchForHits(SearchRequest var1, SearchSourceBuilder var2, BoolQueryBuilder var3, int var4, int var5);

    SearchHits searchForHits(SearchRequest var1, SearchSourceBuilder var2, BoolQueryBuilder var3, int var4, int var5, List<String> var6);

    SearchHits searchForHits(SearchRequest var1, BoolQueryBuilder var2, List<String> var3, int var4, int var5);

    List<Map<String, Object>> scrollSearch(QueryBuilder var1, String... var2);

    List<Map<String, Object>> scrollSearch(QueryBuilder var1, String[] var2, String... var3);

    void scrollSearch(ElasticMethodInterface var1, QueryBuilder var2, String[] var3, String... var4);

    List<Map<String, Object>> scrollSearchAll(ElasticMethodInterface var1, QueryBuilder var2, String[] var3, String... var4);

    List<Map<String, Object>> scrollSearch(SearchRequest var1);

    List<Map<String, Object>> scrollSearch(SearchRequest var1, SearchSourceBuilder var2, BoolQueryBuilder var3, int var4, int var5);

    List<Map<String, Object>> scrollSearch(SearchRequest var1, SearchSourceBuilder var2, BoolQueryBuilder var3, int var4, String var5);

    List<Map<String, Object>> scrollSearch(SearchRequest var1, SearchSourceBuilder var2, BoolQueryBuilder var3, int var4, List<String> var5);

    List<Map<String, Object>> scrollSearch(SearchRequest var1, SearchSourceBuilder var2, BoolQueryBuilder var3, SortBuilder var4, int var5, List<String> var6);
}
