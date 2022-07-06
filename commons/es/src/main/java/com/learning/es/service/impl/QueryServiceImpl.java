package com.learning.es.service.impl;

import com.learning.core.exception.ElasticException;
import com.learning.core.exception.SpringBootException;
import com.learning.core.utils.ArrayUtils;
import com.learning.core.utils.CollectionUtils;
import com.learning.es.bean.SearchResult;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.constants.ElasticMethodInterface;
import com.learning.es.service.EsServiceImpl;
import com.learning.es.service.QueryService;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Log4j2
public class QueryServiceImpl
        extends EsServiceImpl
        implements QueryService {

    /**
     * 注入es链接
     * @param restClientFactory es链接工厂类
     */
    public QueryServiceImpl(RestClientFactory restClientFactory) {
        super(restClientFactory);
    }

    public long count(QueryBuilder queryBuilder, String... indices) {
        //设置查询结果计数器请求
        CountRequest countRequest = new CountRequest(indices);
        countRequest.query(queryBuilder);

        try {
            CountResponse countResponse = this.client.count(countRequest, RequestOptions.DEFAULT);
            return countResponse.getCount();
        } catch (IOException e) {
            throw new ElasticException(e);
        }
    }

    public SearchResult search(QueryBuilder queryBuilder, int size, int from, String... indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页查询条件
        searchSourceBuilder
                .query(queryBuilder)
                .size(size)
                .from(from);

        //执行分页查询请求
        return this.search(searchSourceBuilder, indices);
    }

    public SearchResult search(QueryBuilder queryBuilder, SortBuilder sortBuilder, int size, int from, String... indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder).size(size).from(from);

        if (sortBuilder != null) {
            searchSourceBuilder.sort(sortBuilder);
        }

        return this.search(searchSourceBuilder, indices);
    }

    public SearchResult search(QueryBuilder queryBuilder, String[] fields, int size, int from, String... indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder
                .query(queryBuilder)
                .size(size)
                .from(from);

        if (fields != null && fields.length != 0) {
            searchSourceBuilder.fetchSource(fields, null);
        }

        return this.search(searchSourceBuilder, indices);
    }

    public SearchResult search(SearchSourceBuilder sourceBuilder, String... indices) {
        return getSearchResult(this.searchForHits(sourceBuilder, indices));
    }

    /**
     * 将hits转换为SearchResult
     * @param hits 搜索结果
     * @return
     */
    private SearchResult getSearchResult(SearchHits hits) {
        SearchResult searchResult = new SearchResult();

        if (hits != null) {
            List<Map<String, Object>> result = new ArrayList<>();

            for (SearchHit hit : hits) {
                Map<String, Object> curMap = hit.getSourceAsMap();
                curMap.put("doc_id", hit.getId());
                result.add(curMap);
            }

            searchResult.setResultData(result);
            searchResult.setTotal(hits.getTotalHits().value);
        }

        return searchResult;
    }

    public SearchResult search(SearchRequest searchRequest) {
        return getSearchResult(this.searchForHits(searchRequest));
    }

    public SearchHits searchForHits(SearchSourceBuilder sourceBuilder, String... indices) {

        //设置查询请求
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest
                .types("doc")
                .source(sourceBuilder);

        //获取查询结果
        return this.searchForHits(searchRequest);
    }

    public SearchHits searchForHits(SearchSourceBuilder sourceBuilder, HighlightBuilder highlightBuilder, int from, int size, String... indices) {
        //设置查询请求
        sourceBuilder.from(from)
                .size(size)
                .highlighter(highlightBuilder);

        return this.searchForHits(sourceBuilder, indices);
    }

    public SearchHits searchForHits(SearchRequest searchRequest) {

        try {
            //执行查询请求
            SearchResponse searchResponse = this
                    .client
                    .search(searchRequest, RequestOptions.DEFAULT);
            //返回查询查询数据
            return searchResponse.getHits();
        } catch (Exception e) {
            throw new ElasticException(e);
        }
    }

    public SearchHits searchForHits(SearchRequest searchRequest, SearchSourceBuilder sourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size) {

        //设置查询请求
        searchRequest
                .types("doc")
                .source(
                        sourceBuilder
                                .from(from)
                                .size(size)
                                .query(boolQueryBuilder)
                );
        //执行查询请求获取结果
        return this.searchForHits(searchRequest);
    }

    public SearchHits searchForHits(SearchRequest searchRequest, SearchSourceBuilder sourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size, List<String> regNos) {
        searchRequest.types("doc");
        sourceBuilder
                .from(from)
                .size(size)
                .query(boolQueryBuilder);

        //指定查询的路由分片
        searchRequest.routing(regNos.toArray(new String[0]))
                .source(sourceBuilder);

        return this.searchForHits(searchRequest);
    }

    public SearchHits searchForHits(SearchRequest searchRequest, BoolQueryBuilder boolQueryBuilder, List<String> regNos, int from, int size) {
        searchRequest.types("doc")
                .routing(regNos.toArray(new String[0]))
                .source((new SearchSourceBuilder())
                        .from(from)
                        .size(size)
                        .query(boolQueryBuilder));

        return this.searchForHits(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(QueryBuilder queryBuilder, String... indices) {
        return this.scrollSearch(queryBuilder, null, indices);
    }

    public List<Map<String, Object>> scrollSearch(QueryBuilder queryBuilder, String[] fields, String... indices) {
        return this.scrollSearch(null, true, queryBuilder, fields, indices);
    }


    public void scrollSearch(ElasticMethodInterface methodInterface, QueryBuilder queryBuilder, String[] fields, String... indices) {
        queryBuilder = QueryBuilders.constantScoreQuery(queryBuilder);
        this.scrollSearch(methodInterface, false, queryBuilder, fields, indices);
    }

    public List<Map<String, Object>> scrollSearchAll(ElasticMethodInterface methodInterface, QueryBuilder queryBuilder, String[] fields, String... indices) {
        return this.scrollSearch(methodInterface, false, queryBuilder, fields, indices);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (searchRequest != null && !searchRequest.source().toString().equals("{}")) {
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(5L));
            searchRequest.scroll(scroll);
            searchRequest.types("doc");

            try {
                SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
                SearchHit[] searchHits = searchResponse.getHits().getHits();

                for(SearchHit searchHit : searchHits) {
                    Map<String, Object> curMap = searchHit.getSourceAsMap();
                    curMap.put("doc_id", searchHit.getId());
                    result.add(curMap);
                }

                String scrollId = searchResponse.getScrollId();

                //滚动查询获取下一结果
                while(! ArrayUtils.isEmpty(searchHits)) {
                    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                    scrollRequest.scroll(scroll);

                    try {
                        searchResponse = this.client.scroll(scrollRequest, RequestOptions.DEFAULT);
                        scrollId = searchResponse.getScrollId();
                        searchHits = searchResponse.getHits().getHits();

                        for(SearchHit hit : searchHits) {
                            Map<String, Object> curMap = hit.getSourceAsMap();
                            if (! CollectionUtils.isEmpty(curMap)) {
                                curMap.put("doc_id", hit.getId());
                                result.add(curMap);
                            }
                        }

                    } catch (IOException e) {
                        log.error("es error", e);
                    }
                }

                ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                clearScrollRequest.addScrollId(scrollId);
                ClearScrollResponse clearScrollResponse = this.client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

            } catch (IOException e) {
                log.error("es error", e);
            }

        }
        return result;
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size) {
        searchRequest.types("doc");
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        return this.scrollSearch(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int size, String regNo) {
        searchSourceBuilder.query(boolQueryBuilder).size(size);

        searchRequest.types("doc")
                .routing(regNo)
                .source(searchSourceBuilder);

        return this.scrollSearch(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int size, List<String> regNos) {
        searchRequest.types("doc");

        searchRequest
                .source(searchSourceBuilder
                        .size(size)
                        .query(boolQueryBuilder))
                .routing(regNos
                        .toArray(new String[0]));

        return this.scrollSearch(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, SortBuilder sortBuilder, int size, List<String> regNos) {
        searchRequest.types("doc");

        searchRequest
                .source(searchSourceBuilder
                        .size(size)
                        .query(boolQueryBuilder)
                        .sort(sortBuilder));

        return this.scrollSearch(searchRequest);
    }

    /**
     * 执行滚动查询
     * @param methodInterface 结果处理方法接口
     * @param isReturnAll 是否返回全部查询结果
     * @param queryBuilder 条件构造器
     * @param fields 过滤的源字段
     * @param indices 索引数组
     * @return
     */
    private List<Map<String, Object>> scrollSearch(ElasticMethodInterface<Map<String, Object>> methodInterface, boolean isReturnAll, QueryBuilder queryBuilder, String[] fields, String... indices) {

        List<Map<String, Object>> result = isReturnAll ? new ArrayList<>() : null;
        //启动搜索请求的滚动（数值为活动时间）
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(3L));
        //获取查询条件
        SearchRequest searchRequest = this.getRequestForScroll(queryBuilder, fields, indices);
        searchRequest.scroll(scroll);
        searchRequest.types("doc");

        try {
            SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            for(SearchHit searchHit : searchHits) {
                Map<String, Object> curMap = searchHit.getSourceAsMap();
                curMap.put("doc_id", searchHit.getId());

                //利用methodInterface对查询结果数据进行处理
                if (methodInterface != null) {
                    methodInterface.run(curMap);
                }

                //是否返回全部查询数据
                if (isReturnAll) {
                    result.add(curMap);
                }
            }

            String scrollId = searchResponse.getScrollId();

            //设置数据滚动
            while(! ArrayUtils.isEmpty(searchHits)) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);

                try {
                    searchResponse = this.client.scroll(scrollRequest, RequestOptions.DEFAULT);
                    scrollId = searchResponse.getScrollId();

                    for(SearchHit hit : searchResponse.getHits().getHits()) {
                        Map<String, Object> curMap = hit.getSourceAsMap();

                        if (! CollectionUtils.isEmpty(curMap)) {
                            curMap.put("doc_id", hit.getId());

                            if (methodInterface != null) {
                                methodInterface.run(curMap);
                            }

                            if (isReturnAll) {
                                result.add(curMap);
                            }
                        }
                    }

                } catch (IOException e) {
                    log.error("es error", e);
                }
            }

            //清楚该滚动搜索请求
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = this.client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);

        } catch (InterruptedException | IOException e) {
            log.error("es error", e);
        }

        return result;
    }

    /**
     * 获取滚动请求
     * @param queryBuilder 条件构造器
     * @param fields 过滤的源字段
     * @param indices 索引数组
     * @return
     */
    private SearchRequest getRequestForScroll(QueryBuilder queryBuilder, String[] fields, String... indices) {
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(5000);

        //设置查询字段
        if (fields != null && fields.length != 0) {
            searchSourceBuilder.fetchSource(fields, null);
        }

        searchRequest.types("doc");
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
