package com.learning.es.service.impl;

import com.boot.commons.utils.StringUtils;
import com.boot.form.query.FormElasticManager;
import com.boot.form.query.constants.ElasticConst;
import com.boot.form.query.model.*;
import com.boot.form.query.service.FormQueryService;
import com.dhcc.mrp.common.constant.ConfigConst;
import com.dhcc.mrp.common.constant.ElasticConstant;
import com.dhcc.mrp.common.exceptions.ElasticException;
import com.dhcc.mrp.common.models.elastic.ConfigProperties;
import com.dhcc.mrp.elastic.ElasticManager;
import com.dhcc.mrp.elastic.model.SearchResult;
import com.dhcc.mrp.elastic.profile.clients.RestClientFactory;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.collapse.CollapseBuilder;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单es查询 service实现类
 *
 * @author ：wangpenghui
 * @date ：Created in 2021/3/10 11:17
 */
public class FormQueryServiceImpl implements FormQueryService {

    /**
     * 分页查询
     *
     * @param queryBuilder 查询条件
     * @param page         页码
     * @param size         每页数量
     * @param indices      索引
     * @return
     */
    @Override
    public Map<String, Object> search(QueryBuilder queryBuilder, int page, int size, String... indices) {
        Map<String, Object> result = new HashMap<>();
        if (queryBuilder == null) {
            return result;
        }

        int form = (page - 1) * size;
        SearchResult searchResult = ElasticManager.query().search(queryBuilder, size, form, indices);
        result.put("data", searchResult.getResultData());
        result.put("total", searchResult.getTotal());
        return result;
    }

    /**
     * 分页获取指定表单填写数据
     *
     * @param queryParam
     * @param page
     * @param size
     * @param indices
     * @return
     */
    @Override
    public List<Map<String, Object>> search(QueryParam queryParam, int page, int size, String... indices) {
        QueryBuilder rootQueryBuilder = getRootQueryBuilder(queryParam.getQueryJson());
        return search(rootQueryBuilder, queryParam, page, size, indices);
    }

    /**
     * 分页获取指定表单填写数据
     *
     * @param rootQueryBuilder 一级表查询条件
     * @param queryParam   查询参数
     * @param page         页码
     * @param size         每页数量
     * @param indices      索引
     * @return
     */
    @Override
    public List<Map<String, Object>> search(QueryBuilder rootQueryBuilder, QueryParam queryParam, int page, int size, String... indices) {
        // 查询一级表
        Map<String, Object> patientResult = search(rootQueryBuilder, page, size, indices);
        if (CollectionUtils.isEmpty(patientResult)) {
            return null;
        }
        List<CRFFieldInfo> crfFieldInfoList = queryParam.getCrfFieldInfos();
        List<String> formGuids = crfFieldInfoList.stream().map(CRFFieldInfo::getFormGuid).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(formGuids)) {
            return null;
        }

        List<Map<String, Object>> data = (List<Map<String, Object>>) patientResult.get("data");
        List<String> empis = new ArrayList<>();
        // 1、获取病人登记号信息
        for (Map<String, Object> curMap : data) {
            String empi = curMap.get(ElasticConst.ELASTIC_FIELD_EMPI) == null
                    ? "" : curMap.get(ElasticConst.ELASTIC_FIELD_EMPI).toString();
            if (!StringUtils.isEmpty(empi)) {
                empis.add(empi);
            }
        }

        // 2、获取指定登记号指定表单es信息
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termsQuery(ElasticConst.ELASTIC_FIELD_EMPI, empis));
        // 过滤指定formguid表单数据
        BoolQueryBuilder filter = new BoolQueryBuilder();
        filter.should(QueryBuilders.termsQuery(ElasticConst.ELASTIC_FIELD_FORM_GUID, formGuids))
                .should(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, ElasticConst.ELASTIC_FIELD_PATIENT))
                .should(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, ElasticConst.ELASTIC_FIELD_ATTACHMENT));
        filter.minimumShouldMatch(1);
        boolQueryBuilder.filter(filter);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(indices)
                .source(searchSourceBuilder)
                .routing(empis.toArray(new String[0]));
        List<Map<String, Object>> searchResult = ElasticManager.query().scrollSearch(searchRequest);

        return searchResult;
    }

    /**
     * 分页折叠查询
     * 对病人数据去重查询
     *
     * @param queryBuilder 查询条件
     * @param page         页码
     * @param size         每页数量
     * @param indices      索引
     * @return
     */
    @Override
    public Map<String, Object> collapseSearch(QueryBuilder queryBuilder, int page, int size, String... indices) {
        Map<String, Object> result = new HashMap<>();
        if (queryBuilder == null) {
            return result;
        }

        int form = (page - 1) * size;
        // 获取es高级客户端
        RestHighLevelClient client = RestClientFactory.getInstance(ElasticManager.class,
                ConfigProperties.getKey(ConfigConst.ES_NETWORK_ADDRESS)
        ).getRestHighLevelClient();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 对empi去重折叠查询
        searchSourceBuilder.collapse(new CollapseBuilder(ElasticConst.ELASTIC_FIELD_EMPI))
                // 聚合统计病人数量
                .aggregation(AggregationBuilders.cardinality("count").field(ElasticConst.ELASTIC_FIELD_EMPI))
                .from(form)
                .size(size)
                .query(queryBuilder);

        SearchRequest searchRequest = new SearchRequest(indices)
                .types(ElasticConst.ELASTIC_DEFAULT_TYPE_NAME)
                .source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits hits = searchResponse.getHits();
            Cardinality admCountAgg = searchResponse.getAggregations().get("count");
            // 总患者数量
            long total = admCountAgg.getValue();
            if (hits != null) {
                List<Map<String, Object>> data = new ArrayList<>();
                for (SearchHit hit : hits) {
                    Map<String, Object> curMap = hit.getSourceAsMap();
                    curMap.put(ElasticConstant.DEFAULT_ID_FIELD, hit.getId());
                    data.add(curMap);
                }
                result.put("data", data);
                result.put("total", total);
            }
        } catch (Exception e) {
            throw new ElasticException(e);
        }

        return result;
    }

    @Override
    public List<String> searchForEmpi(QueryJson queryJson, String... indices) {
        QueryBuilder rootQueryBuilder = FormElasticManager.query().getRootQueryBuilder(queryJson);
        return searchForEmpi(rootQueryBuilder, indices);
    }

    @Override
    public List<String> searchForEmpi(QueryBuilder rootQueryBuilder, String... indices) {
        Set<String> empis = new HashSet<>();

        // 获取es高级客户端
        RestHighLevelClient client = RestClientFactory.getInstance(ElasticManager.class,
                ConfigProperties.getKey(ConfigConst.ES_NETWORK_ADDRESS)
        ).getRestHighLevelClient();

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

        // 对登记号去重折叠查询
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(new BoolQueryBuilder().filter(rootQueryBuilder))
                .fetchSource(ElasticConst.ELASTIC_FIELD_EMPI, null)
                .size(5000); //设定每次返回多少条数据

        SearchRequest searchRequest = new SearchRequest(indices)
                .types(ElasticConstant.DEFAULT_TYPE_NAME)
                .source(searchSourceBuilder)
                .scroll(scroll);

        SearchResponse searchResponse;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            for (SearchHit searchHit : searchHits) {
                Map<String, Object> curMap = searchHit.getSourceAsMap();
                String empi = curMap.get(ElasticConst.ELASTIC_FIELD_EMPI) == null
                        ? "" : curMap.get(ElasticConst.ELASTIC_FIELD_EMPI).toString();
                if (!StringUtils.isEmpty(empi)) {
                    empis.add(empi);
                }
            }

            //遍历搜索命中的数据，直到没有数据
            String scrollId = searchResponse.getScrollId();
            while (searchHits != null && searchHits.length > 0) {
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId).scroll(scroll);
                searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();
                if (searchHits != null && searchHits.length > 0) {
                    for (SearchHit hit : searchHits) {
                        Map<String, Object> curMap = hit.getSourceAsMap();
                        String empi = curMap.get(ElasticConst.ELASTIC_FIELD_EMPI) == null
                                ? "" : curMap.get(ElasticConst.ELASTIC_FIELD_EMPI).toString();
                        if (!StringUtils.isEmpty(empi)) {
                            empis.add(empi);
                        }
                    }
                }
            }

            //清除滚屏
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            // 也可以选择setScrollIds()将多个scrollId一起使用
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
            boolean succeeded = clearScrollResponse.isSucceeded();
        } catch (IOException e) {
            throw new ElasticException(e);
        }

        return new ArrayList<>(empis);
    }

    /**
     * 获取指定登记号、指定表单es数据
     *
     * @param empis
     * @param queryParam
     * @param indices
     * @return
     */
    @Override
    public List<Map<String, Object>> scrollSearch(List<String> empis, QueryParam queryParam, String... indices) {
        List<CRFFieldInfo> crfFieldInfoList = queryParam.getCrfFieldInfos();
        List<String> formGuids = crfFieldInfoList.stream().map(CRFFieldInfo::getFormGuid).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(formGuids)) {
            return null;
        }

        // 获取指定登记号指定表单es信息
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termsQuery(ElasticConst.ELASTIC_FIELD_EMPI, empis));
        // 过滤指定formguid表单数据
        BoolQueryBuilder filter = new BoolQueryBuilder();
        filter.should(QueryBuilders.termsQuery(ElasticConst.ELASTIC_FIELD_FORM_GUID, formGuids))
                .should(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, ElasticConst.ELASTIC_FIELD_PATIENT))
                .should(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, ElasticConst.ELASTIC_FIELD_ATTACHMENT));
        filter.minimumShouldMatch(1);
        boolQueryBuilder.filter(filter);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder().query(boolQueryBuilder);
        SearchRequest searchRequest = new SearchRequest(indices)
                .source(searchSourceBuilder)
                .routing(empis.toArray(new String[0]));
        return ElasticManager.query().scrollSearch(searchRequest);
    }

    /**
     * 获取纳排查询条件
     *
     * @param queryJson
     * @return
     */
    @Override
    public QueryBuilder getRootQueryBuilder(QueryJson queryJson) {
        Map<String, ConditionBuilder> condMap = QueryConditionGroup.getConditionBuilder(
                queryJson.getBringIntoCondJson(),
                queryJson.getRulingOutCondJson()
        );

        ConditionBuilder bringInto = condMap.get("bringInto");
        ConditionBuilder rulingOut = condMap.get("rulingOut");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (bringInto != null) {
            boolQueryBuilder.must(bringInto.getRootTypeQueryBuider());
        }
        if (rulingOut != null) {
            boolQueryBuilder.mustNot(rulingOut.getRootTypeQueryBuider());
        }

        if (bringInto == null && rulingOut == null){
            boolQueryBuilder.must(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, ElasticConst.ELASTIC_FIELD_PATIENT));
        }
        return boolQueryBuilder;
    }


}

