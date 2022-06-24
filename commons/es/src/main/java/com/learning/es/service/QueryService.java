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

    /**
     * 获取索引计数
     * @param queryBuilder
     * @param indices 索引数组
     * @return
     */
    long count(QueryBuilder queryBuilder, String... indices);

    /**
     * 执行分页查询
     * @param queryBuilder 查询构造器
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    SearchResult search(QueryBuilder queryBuilder, int size, int from, String... indices);

    /**
     * 获取有排序的结果
     * @param queryBuilder 查询构造器
     * @param sortBuilder 排序构造器
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    SearchResult search(QueryBuilder queryBuilder, SortBuilder sortBuilder, int size, int from, String... indices);

    /**
     * 搜索带有对应字段的结果
     * @param queryBuilder 查询构造器
     * @param fields 包含的字段
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    SearchResult search(QueryBuilder queryBuilder, String[] fields, int size, int from, String... indices);

    /**
     * 根据sourceBuilder 查询数据
     * @param sourceBuilder 搜索源构造器
     * @param indices 索引
     * @return
     */
    SearchResult search(SearchSourceBuilder sourceBuilder, String... indices);

    /**
     * 根据搜索请求获取结果
     * @param searchRequest 搜索请求
     * @return
     */
    SearchResult search(SearchRequest searchRequest);

    /**
     * 搜索相应构造器索引下的相关数据
     * @param sourceBuilder 搜索源构造器
     * @param indices 索引
     * @return
     */
    SearchHits searchForHits(SearchSourceBuilder sourceBuilder, String... indices);

    /**
     * 带高亮的查询
     * @param sourceBuilder 查询源构造器
     * @param highlightBuilder 高亮构造器
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    SearchHits searchForHits(SearchSourceBuilder sourceBuilder, HighlightBuilder highlightBuilder, int from, int size, String... indices);

    /**
     * 获取查询的数据
     * @param searchRequest 查询请求
     * @return
     */
    SearchHits searchForHits(SearchRequest searchRequest);

    /**
     * 获取多条件查询结果
     * @param searchRequest 查询请求
     * @param sourceBuilder 查询条件构造器
     * @param boolQueryBuilder 联合查询构造器
     * @param from 起始页
     * @param size 每页数据条数
     * @return
     */
    SearchHits searchForHits(SearchRequest searchRequest, SearchSourceBuilder sourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size);

    /**
     * 指定具体分片出进行多条件查询
     * @param searchRequest 查询请求
     * @param sourceBuilder 查询条件构造器
     * @param boolQueryBuilder 多条件查询构造器
     * @param from 起始页
     * @param size 单页数据量
     * @param regNos
     * @return
     */
    SearchHits searchForHits(SearchRequest searchRequest, SearchSourceBuilder sourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size, List<String> regNos);

    /**
     * 进行指定路由分片的多条件查询
     * @param searchRequest 查询请求
     * @param boolQueryBuilder 多条件查询构造器
     * @param from 起始页
     * @param size 单页数据量
     * @return
     */
    SearchHits searchForHits(SearchRequest searchRequest, BoolQueryBuilder boolQueryBuilder, List<String> regNos, int from, int size);

    /**
     * 根据查询条件对指定索引进行查询
     * @param queryBuilder 查询条件
     * @param indices 索引数组
     * @return
     */
    List<Map<String, Object>> scrollSearch(QueryBuilder queryBuilder, String... indices);

    /**
     * 根据查询条件及索引对指定字段进行查询
     * @param queryBuilder 条件构造器
     * @param fields 指定字段
     * @param indices 索引数组
     * @return
     */
    List<Map<String, Object>> scrollSearch(QueryBuilder queryBuilder, String[] fields, String... indices);

    /**
     * 不返回结果的查询
     * @param methodInterface 查询结果处理函数式接口
     * @param queryBuilder 查询条件构造器
     * @param fields 过滤字段
     * @param indices 索引数组
     */
    void scrollSearch(ElasticMethodInterface methodInterface, QueryBuilder queryBuilder, String[] fields, String... indices);

    /**
     *
     * @param methodInterface 查询结果处理函数式接口
     * @param queryBuilder 查询条件构造器
     * @param fields 过滤字段
     * @param indices 索引数组
     * @return
     */
    List<Map<String, Object>> scrollSearchAll(ElasticMethodInterface methodInterface, QueryBuilder queryBuilder, String[] fields, String... indices);

    /**
     * 根据查询请求进行滚动查询
     * @param searchRequest 查询请求
     * @return
     */
    List<Map<String, Object>> scrollSearch(SearchRequest searchRequest);

    /**
     * 执行滚动请求
     * @param searchRequest 搜索请求
     * @param searchSourceBuilder 条件构造器
     * @param boolQueryBuilder 多条件构造器
     * @param from 起始页
     * @param size 每页数据数
     * @return
     */
    List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size);

    /**
     * 执行分片路由滚动请求
     * @param searchRequest 搜索请求
     * @param searchSourceBuilder 条件构造器
     * @param boolQueryBuilder 多条件构造器
     * @param size 每页数据数
     * @param regNo 分片路由
     * @return
     */
    List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int size, String regNo);

    /**
     * 执行多分片路由滚动请求
     * @param searchRequest 搜索请求
     * @param searchSourceBuilder 条件构造器
     * @param boolQueryBuilder 多条件构造器
     * @param size 每页数据数
     * @param regNos 分片路由数组
     * @return
     */
    List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int size, List<String> regNos);

    /**
     * 带排序的分片的滚动排序
     * @param searchRequest 搜索请求
     * @param searchSourceBuilder 条件构造器
     * @param boolQueryBuilder 多条件构造器
     * @param sortBuilder 排序条件构造器
     * @param size 每页数据数
     * @param regNos 分片路由数组
     * @return
     */
    List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, SortBuilder sortBuilder, int size, List<String> regNos);
}
