package com.learning.es.service.impl;

import com.learning.core.exception.ElasticException;
import com.learning.core.exception.SpringBootException;
import com.learning.es.bean.SearchResult;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.constants.ElasticMethodInterface;
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
public class QueryServiceImpl implements QueryService {
    protected RestHighLevelClient client;
    protected RestClient restClient;

    /**
     * 注入es链接
     * @param restClientFactory es链接工厂类
     */
    public QueryServiceImpl(RestClientFactory restClientFactory) {
        if (restClientFactory == null) {
            throw new SpringBootException("ES RestClient 为空， 请检查ES连接");
        } else {
            this.client = restClientFactory.getRestHighLevelClient();
            this.restClient = restClientFactory.getRestClient();
        }
    }

    /**
     * 获取索引计数
     * @param queryBuilder
     * @param indices 索引数组
     * @return
     */
    public long count(QueryBuilder queryBuilder, String... indices) {
        //设置索引计数器
        CountRequest countRequest = new CountRequest(indices);
        countRequest.query(queryBuilder);

        try {
            CountResponse countResponse = this.client.count(countRequest, RequestOptions.DEFAULT);
            return countResponse.getCount();
        } catch (IOException e) {
            throw new ElasticException(e);
        }
    }

    /**
     * 执行分页查询
     * @param queryBuilder 查询构造器
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    public SearchResult search(QueryBuilder queryBuilder, int size, int from, String... indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置分页查询条件
        searchSourceBuilder.query(queryBuilder).size(size).from(from);
        return this.search(searchSourceBuilder, indices);
    }

    /**
     * 获取有排序的结果
     * @param queryBuilder 查询构造器
     * @param sortBuilder 排序构造器
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    public SearchResult search(QueryBuilder queryBuilder, SortBuilder sortBuilder, int size, int from, String... indices) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder).size(size).from(from);

        if (sortBuilder != null) {
            searchSourceBuilder.sort(sortBuilder);
        }

        return this.search(searchSourceBuilder, indices);
    }

    /**
     * 搜索带有对应字段的结果
     * @param queryBuilder 查询构造器
     * @param fields 包含的字段
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
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

    /**
     * 根据sourceBuilder 查询数据
     * @param sourceBuilder 搜索源构造器
     * @param indices 索引
     * @return
     */
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

    /**
     * 根据搜索请求获取结果
     * @param searchRequest 搜索请求
     * @return
     */
    public SearchResult search(SearchRequest searchRequest) {
        return getSearchResult(this.searchForHits(searchRequest));
    }

    /**
     * 搜索相应构造器索引下的相关数据
     * @param sourceBuilder 搜索源构造器
     * @param indices 索引
     * @return
     */
    public SearchHits searchForHits(SearchSourceBuilder sourceBuilder, String... indices) {
        SearchRequest searchRequest = new SearchRequest(indices);
        //设置类型过滤条件
        searchRequest.types("doc");
        searchRequest.source(sourceBuilder);
        return this.searchForHits(searchRequest);
    }

    /**
     * 带高亮的查询
     * @param sourceBuilder 查询源构造器
     * @param highlightBuilder 高亮构造器
     * @param size 查询数据的条数，默认为 10；
     * @param from 查询数据开始的位置，默认为0；
     * @param indices 索引
     * @return
     */
    public SearchHits searchForHits(SearchSourceBuilder sourceBuilder, HighlightBuilder highlightBuilder, int from, int size, String... indices) {
        sourceBuilder.from(from)
                .size(size)
                .highlighter(highlightBuilder);

        return this.searchForHits(sourceBuilder, indices);
    }

    /**
     * 获取查询的数据
     * @param searchRequest 查询请求
     * @return
     */
    public SearchHits searchForHits(SearchRequest searchRequest) {

        try {
            SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
            //返回查询查询数据
            return searchResponse.getHits();
        } catch (Exception e) {
            throw new ElasticException(e);
        }
    }

    /**
     *
     * @param searchRequest
     * @param sourceBuilder
     * @param boolQueryBuilder
     * @param from
     * @param size
     * @return
     */
    public SearchHits searchForHits(SearchRequest searchRequest, SearchSourceBuilder sourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size) {
        searchRequest.types("doc");
        sourceBuilder.from(from);
        sourceBuilder.size(size);
        sourceBuilder.query(boolQueryBuilder);
        searchRequest.source(sourceBuilder);
        return this.searchForHits(searchRequest);
    }

    public SearchHits searchForHits(SearchRequest searchRequest, SearchSourceBuilder sourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size, List<String> regNos) {
        searchRequest.types(new String[]{"doc"});
        sourceBuilder.from(from).size(size).query(boolQueryBuilder);
        searchRequest.routing((String[])regNos.toArray(new String[0])).source(sourceBuilder);
        return this.searchForHits(searchRequest);
    }

    public SearchHits searchForHits(SearchRequest searchRequest, BoolQueryBuilder boolQueryBuilder, List<String> regNos, int from, int size) {
        searchRequest.types(new String[]{"doc"}).routing((String[])regNos.toArray(new String[0])).source((new SearchSourceBuilder()).from(from).size(size).query(boolQueryBuilder));
        return this.searchForHits(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(QueryBuilder queryBuilder, String... indices) {
        return this.scrollSearch(queryBuilder, (String[])null, indices);
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
        List<Map<String, Object>> result = new ArrayList();
        if (searchRequest != null && !searchRequest.source().toString().equals("{}")) {
            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(5L));
            searchRequest.scroll(scroll);
            searchRequest.types(new String[]{"doc"});

            try {
                SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
                SearchHit[] searchHits = searchResponse.getHits().getHits();
                SearchHit[] var6 = searchHits;
                int var7 = searchHits.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    SearchHit searchHit = var6[var8];
                    Map<String, Object> curMap = searchHit.getSourceAsMap();
                    curMap.put("doc_id", searchHit.getId());
                    result.add(curMap);
                }

                String scrollId = searchResponse.getScrollId();

                while(searchHits != null && searchHits.length > 0) {
                    SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                    scrollRequest.scroll(scroll);

                    try {
                        searchResponse = this.client.scroll(scrollRequest, RequestOptions.DEFAULT);
                        scrollId = searchResponse.getScrollId();
                        searchHits = searchResponse.getHits().getHits();
                        if (searchHits != null && searchHits.length > 0) {
                            SearchHit[] var18 = searchHits;
                            int var20 = searchHits.length;

                            for(int var22 = 0; var22 < var20; ++var22) {
                                SearchHit hit = var18[var22];
                                Map<String, Object> curMap = hit.getSourceAsMap();
                                if (curMap == null || curMap.keySet().size() != 0) {
                                    curMap.put("doc_id", hit.getId());
                                    result.add(curMap);
                                }
                            }
                        }
                    } catch (IOException var13) {
                        this.log.error("es error", var13);
                    }
                }

                ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                clearScrollRequest.addScrollId(scrollId);
                ClearScrollResponse clearScrollResponse = this.client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
                boolean var21 = clearScrollResponse.isSucceeded();
            } catch (IOException var14) {
                this.log.error("es error", var14);
            }

            return result;
        } else {
            return result;
        }
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int from, int size) {
        searchRequest.types(new String[]{"doc"});
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        return this.scrollSearch(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int size, String regNo) {
        searchSourceBuilder.query(boolQueryBuilder).size(size);
        searchRequest.types(new String[]{"doc"}).routing(regNo).source(searchSourceBuilder);
        return this.scrollSearch(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, int size, List<String> regNos) {
        searchRequest.types(new String[]{"doc"});
        searchRequest.source(searchSourceBuilder.size(size).query(boolQueryBuilder)).routing((String[])regNos.toArray(new String[0]));
        return this.scrollSearch(searchRequest);
    }

    public List<Map<String, Object>> scrollSearch(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, BoolQueryBuilder boolQueryBuilder, SortBuilder sortBuilder, int size, List<String> regNos) {
        searchRequest.types(new String[]{"doc"});
        searchRequest.source(searchSourceBuilder.size(size).query(boolQueryBuilder).sort(sortBuilder));
        return this.scrollSearch(searchRequest);
    }

    private List<Map<String, Object>> scrollSearch(ElasticMethodInterface methodInterface, boolean isReturnAll, QueryBuilder queryBuilder, String[] fields, String... indices) {
        List<Map<String, Object>> result = isReturnAll ? new ArrayList() : null;
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(3L));
        SearchRequest searchRequest = this.getRequestForScroll(queryBuilder, fields, indices);
        searchRequest.scroll(scroll);
        searchRequest.types(new String[]{"doc"});

        try {
            SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            SearchHit[] var11 = searchHits;
            int var12 = searchHits.length;

            for(int var13 = 0; var13 < var12; ++var13) {
                SearchHit searchHit = var11[var13];
                Map<String, Object> curMap = searchHit.getSourceAsMap();
                curMap.put("doc_id", searchHit.getId());
                if (methodInterface != null) {
                    methodInterface.run(curMap);
                }

                if (isReturnAll) {
                    result.add(curMap);
                }
            }

            String scrollId = searchResponse.getScrollId();

            while(searchHits != null && searchHits.length > 0) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);

                try {
                    searchResponse = this.client.scroll(scrollRequest, RequestOptions.DEFAULT);
                    scrollId = searchResponse.getScrollId();
                    searchHits = searchResponse.getHits().getHits();
                    if (searchHits != null && searchHits.length > 0) {
                        SearchHit[] var23 = searchHits;
                        int var25 = searchHits.length;

                        for(int var27 = 0; var27 < var25; ++var27) {
                            SearchHit hit = var23[var27];
                            Map<String, Object> curMap = hit.getSourceAsMap();
                            if (curMap == null || curMap.keySet().size() != 0) {
                                curMap.put("doc_id", hit.getId());
                                if (methodInterface != null) {
                                    methodInterface.run(curMap);
                                }

                                if (isReturnAll) {
                                    result.add(curMap);
                                }
                            }
                        }
                    }
                } catch (IOException var18) {
                    this.log.error("es error", var18);
                }
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = this.client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean var26 = clearScrollResponse.isSucceeded();
        } catch (InterruptedException | IOException var19) {
            this.log.error("es error", var19);
        }

        return result;
    }

    private SearchRequest getRequestForScroll(QueryBuilder queryBuilder, String[] fields, String... indices) {
        SearchRequest searchRequest = new SearchRequest(indices);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        searchSourceBuilder.size(5000);
        if (fields != null && fields.length != 0) {
            searchSourceBuilder.fetchSource(fields, (String[])null);
        }

        searchRequest.types(new String[]{"doc"});
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
