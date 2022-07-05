package com.learning.es.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.learning.core.utils.DateTimeUtil;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.condition.ConditionBuilder;
import com.learning.es.service.DocumentService;
import com.learning.es.service.EsServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class DocumentServiceImpl
        extends EsServiceImpl
        implements DocumentService {

    /**
     * 文档单次批量处理数据量
     */
    final int batchCount = 5000;

    public DocumentServiceImpl(RestClientFactory restClientFactory) {

        super(restClientFactory);
    }

    /**
     * 构造批量查插入请求
     * @param index 索引
     * @param actions 批量对应的操作数组
     */
    public void bulkInsert(String index, List<Map<String, Object>> actions) {
        String routingField = "_routing";
        String sourceIdField = "source_id";
        String docIdField = "doc_id";
        String type = "doc";
        String timestamp = "timestamp";
        BulkRequest bulkRequest = new BulkRequest();
        int count = 0;
        int total = actions.size();
        Iterator<Map<String, Object>> actionsIt = actions.iterator();

        while(true) {
            //设置批量操作请求
            do {
                Map<String, Object> map;
                String docId;
                String routing;

                do {
                    do {
                        if (! actionsIt.hasNext()) {
                            return;
                        }

                        map = actionsIt.next();
                        String sourceId = map.getOrDefault(sourceIdField, "").toString();
                        //获取文档id
                        docId = map.getOrDefault(docIdField, "").toString();
                        //获取存储的分片路由
                        routing = map.getOrDefault(routingField, "").toString();
                    } while(StringUtils.isEmpty(routing));

                } while(StringUtils.isEmpty(docId));

                map.remove(docIdField);
                map.remove(routingField);
                //设置时间字段
                map.put(timestamp, DateTimeUtil.getNowTimeStr());
                bulkRequest
                        .add(
                                (new IndexRequest(index))
                                .id(docId)
                                .routing(routing)
                                .type(type)
                                .source(map)
                        );
                ++count;
            } while(count % 5000 != 0 && count < total);

            BulkResponse cBulkResponse = null;

            //发送批量插入请求
            try {
                cBulkResponse = this.client.bulk(bulkRequest, RequestOptions.DEFAULT);

                if (cBulkResponse != null && cBulkResponse.hasFailures()) {
                    log.error(cBulkResponse.buildFailureMessage());
                } else {
                    log.info("成功插入5000条");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            bulkRequest = new BulkRequest();
        }
    }

    /**
     *
     * @param sourceIndex
     * @param destIndex
     * @param bringInto 查询中必须包含的项
     * @param rulingOut 查询中肯定不包含的项
     * @param dateQuery 数据查询构造器
     */
    public void bulkByQuery(
            String sourceIndex,
            String destIndex,
            ConditionBuilder bringInto,
            ConditionBuilder rulingOut,
            QueryBuilder dateQuery
    ) {
        String regnoField = "regno";
        Integer batchCount = 5000;

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder patientQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder secondQueryBuilder = new BoolQueryBuilder();
        BoolQueryBuilder thirdQueryBuilder = new BoolQueryBuilder();

        //设置必须包含的搜索字段
        if (bringInto != null) {
            patientQueryBuilder.must(bringInto.toQueryBuilderForPatient(1));
            secondQueryBuilder.must(bringInto.toQueryBuilderForPatient(2));
            thirdQueryBuilder.must(bringInto.toQueryBuilderForPatient(3));
        }

        //设置排除数据
        if (rulingOut != null) {
            patientQueryBuilder.mustNot(rulingOut.toQueryBuilderForPatient(1));
            secondQueryBuilder.mustNot(rulingOut.toQueryBuilderForPatient(2));
            thirdQueryBuilder.mustNot(rulingOut.toQueryBuilderForPatient(3));
        }

        //注入数据搜索构造器
        if (dateQuery != null) {
            patientQueryBuilder.must(dateQuery);
            secondQueryBuilder.must(dateQuery);
            thirdQueryBuilder.must(dateQuery);
        }

        boolQueryBuilder
                .should(patientQueryBuilder)
                .should(secondQueryBuilder)
                .should(thirdQueryBuilder);

        //设置最小满足项为1
        boolQueryBuilder.minimumShouldMatch(1);
        List<Map<String, Object>> result = new ArrayList<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.constantScoreQuery(boolQueryBuilder)).size(batchCount);
        SearchRequest searchRequest = (new SearchRequest(new String[]{sourceIndex})).source(sourceBuilder).types(new String[]{"doc"});
        CountRequest countRequest = (new CountRequest(new String[]{sourceIndex})).source((new SearchSourceBuilder()).query(boolQueryBuilder));
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(5L));
        searchRequest.scroll(scroll);

        try {
            Integer count = 0;
            CountResponse countResponse = this.client.count(countRequest, RequestOptions.DEFAULT);
            Long total = countResponse.getCount();
            SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = searchResponse.getHits().getHits();
            SearchHit[] var22 = searchHits;
            int var23 = searchHits.length;

            for(int var24 = 0; var24 < var23; ++var24) {
                SearchHit searchHit = var22[var24];
                Map<String, Object> curMap = searchHit.getSourceAsMap();
                String routing = curMap.get(regnoField) == null ? "" : curMap.get(regnoField).toString();
                curMap.put("doc_id", searchHit.getId());
                curMap.put("_routing", routing);
                result.add(curMap);
                count = count + 1;
            }

            this.bulk(destIndex, result);
            result.clear();
            String scrollId = searchResponse.getScrollId();

            while(true) {
                do {
                    do {
                        if (searchHits == null || searchHits.length <= 0) {
                            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                            clearScrollRequest.addScrollId(scrollId);
                            this.client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
                            return;
                        }

                        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                        scrollRequest.scroll(scroll);
                        searchResponse = this.client.scroll(scrollRequest, RequestOptions.DEFAULT);
                        scrollId = searchResponse.getScrollId();
                        searchHits = searchResponse.getHits().getHits();
                    } while(searchHits == null);
                } while(searchHits.length <= 0);

                SearchHit[] var36 = searchHits;
                int var38 = searchHits.length;

                for(int var40 = 0; var40 < var38; ++var40) {
                    SearchHit hit = var36[var40];
                    Map<String, Object> curMap = hit.getSourceAsMap();
                    String routing = curMap.get(regnoField) == null ? "" : curMap.get(regnoField).toString();
                    curMap.put("doc_id", hit.getId());
                    curMap.put("_routing", routing);
                    result.add(curMap);
                    count = count + 1;
                }

                this.bulk(destIndex, result);
                result.clear();
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setMaximumFractionDigits(2);
                String progress = numberFormat.format((double)((float)count / (float)total * 100.0F));
                System.out.println("专病采集任务进度： " + progress + "%");
            }
        } catch (IOException var32) {
            var32.printStackTrace();
        }
    }

    public void bulkByCaseIds(String sourceIndex, String destIndex, List<String> caseIds) {
        this.bulkByCaseIds((ElasticMethodInterface)null, sourceIndex, destIndex, caseIds, (String)null, (String)null);
    }

    public void bulkByCaseIds(String sourceIndex, String destIndex, List<String> caseIds, String lastTime, String nextTime) {
        this.bulkByCaseIds((ElasticMethodInterface)null, sourceIndex, destIndex, caseIds, lastTime, nextTime);
    }

    public void bulkByCaseIds(ElasticMethodInterface methodInterface, String sourceIndex, String destIndex, List<String> caseIds, String lastTime, String nextTime) {
        String regnoField = "regno";
        String timestampField = "timestamp";
        RangeQueryBuilder dateQuery = null;
        if (StringUtil.isNotEmpty(lastTime)) {
            dateQuery = QueryBuilders.rangeQuery(timestampField).gte(lastTime);
        }

        if (StringUtil.isNotEmpty(nextTime)) {
            dateQuery = dateQuery == null ? QueryBuilders.rangeQuery(timestampField).lt(nextTime) : dateQuery.lt(nextTime);
        }

        if (caseIds != null && caseIds.size() > 0) {
            int count = 0;
            int totalCount = 0;
            int total = caseIds.size();
            List<String> regnos = new ArrayList();
            Iterator var14 = caseIds.iterator();

            while(true) {
                do {
                    if (!var14.hasNext()) {
                        return;
                    }

                    String caseId = (String)var14.next();
                    ++count;
                    ++totalCount;
                    regnos.add(caseId);
                } while(count % 100 != 0 && totalCount != total);

                List<Map<String, Object>> result = new ArrayList();
                BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
                QueryBuilder termsQuery = new TermsQueryBuilder(regnoField, regnos);
                boolQueryBuilder.filter(termsQuery);
                if (dateQuery != null) {
                    boolQueryBuilder.filter(dateQuery);
                }

                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.constantScoreQuery(boolQueryBuilder)).size(5000);
                SearchRequest searchRequest = (new SearchRequest(new String[]{sourceIndex})).source(sourceBuilder).types(new String[]{"doc"});
                Scroll scroll = new Scroll(TimeValue.timeValueMinutes(5L));
                searchRequest.scroll(scroll);

                try {
                    SearchResponse searchResponse = this.client.search(searchRequest, RequestOptions.DEFAULT);
                    SearchHit[] searchHits = searchResponse.getHits().getHits();
                    SearchHit[] var24 = searchHits;
                    int var25 = searchHits.length;

                    for(int var26 = 0; var26 < var25; ++var26) {
                        SearchHit searchHit = var24[var26];
                        Map<String, Object> curMap = searchHit.getSourceAsMap();
                        String routing = curMap.get(regnoField) == null ? "" : curMap.get(regnoField).toString();
                        curMap.put("doc_id", searchHit.getId());
                        curMap.put("_routing", routing);
                        if (methodInterface != null) {
                            methodInterface.run(curMap);
                        }

                        result.add(curMap);
                    }

                    String scrollId = searchResponse.getScrollId();

                    while(searchHits != null && searchHits.length > 0) {
                        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                        scrollRequest.scroll(scroll);

                        try {
                            searchResponse = this.client.scroll(scrollRequest, RequestOptions.DEFAULT);
                        } catch (IOException var32) {
                            var32.printStackTrace();
                        }

                        scrollId = searchResponse.getScrollId();
                        searchHits = searchResponse.getHits().getHits();
                        if (searchHits != null && searchHits.length > 0) {
                            SearchHit[] var37 = searchHits;
                            int var38 = searchHits.length;

                            for(int var39 = 0; var39 < var38; ++var39) {
                                SearchHit hit = var37[var39];
                                Map<String, Object> curMap = hit.getSourceAsMap();
                                String routing = curMap.get(regnoField) == null ? "" : curMap.get(regnoField).toString();
                                curMap.put("doc_id", hit.getId());
                                curMap.put("_routing", routing);
                                if (methodInterface != null) {
                                    methodInterface.run(curMap);
                                }

                                result.add(curMap);
                            }
                        }
                    }

                    ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
                    clearScrollRequest.addScrollId(scrollId);
                    this.client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
                } catch (InterruptedException | IOException var33) {
                    var33.printStackTrace();
                }

                this.bulk(destIndex, result);
                regnos.clear();
            }
        }
    }

    public void updateByDocId(String index, String docId, String routing, String jsonString) {
        if (!StringUtil.isEmpty(jsonString)) {
            String type = "doc";
            UpdateRequest request = ((UpdateRequest)(new UpdateRequest()).index(index)).id(docId).routing(routing).type(type).doc(jsonString, XContentType.JSON).docAsUpsert(true);
            UpdateResponse updateResponse = null;

            try {
                updateResponse = this.client.update(request, RequestOptions.DEFAULT);
            } catch (IOException var9) {
                var9.printStackTrace();
            }

            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("elastic文档新增成功");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("elastic文档相关字段数据修改成功");
            }

        }
    }

    public void updateByDocId(String index, String docId, String routing, Map data) {
        String type = "doc";
        UpdateRequest request = ((UpdateRequest)(new UpdateRequest()).index(index)).id(docId).routing(routing).type(type).doc(data).docAsUpsert(true);
        UpdateResponse updateResponse = null;

        try {
            updateResponse = this.client.update(request, RequestOptions.DEFAULT);
        } catch (IOException var9) {
            var9.printStackTrace();
        }

        if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
            System.out.println("elastic文档新增成功");
        } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            System.out.println("elastic文档相关字段数据修改成功");
        }

    }

    public void updateCRFFillData(String index, String docId, String routing, String jsonString, String fieldName) {
        if (!StringUtil.isEmpty(jsonString)) {
            String type = "doc";
            String routingField = "_routing";
            String elasticRowField = "row_field";
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            JSONObject crfFillDataObj = jsonObject.getJSONObject(fieldName);
            String rowField = crfFillDataObj.getString(elasticRowField);
            Map<String, Object> curResult = null;
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(new String[]{type}).addIds(new String[]{docId});
            SearchSourceBuilder searchSourceBuilder = (new SearchSourceBuilder()).query(idsQueryBuilder);
            SearchRequest searchRequest = (new SearchRequest(new String[]{index})).source(searchSourceBuilder);
            List<Map<String, Object>> result = ElasticManager.query().scrollSearch(searchRequest);
            if (result != null && result.size() > 0) {
                Map<String, Object> curMap = (Map)result.get(0);
                curResult = this.getCRFTableData(curMap, fieldName, rowField, crfFillDataObj);
            }

            if (curResult != null) {
                this.updateByDocId(index, docId, routing, curResult);
            } else {
                this.updateByDocId(index, docId, routing, jsonString);
            }

        }
    }

    public void updateCRFFillDatas(String index, List<ElasticCRFFillData> elasticCRFFillDatas) {
        if (elasticCRFFillDatas != null && elasticCRFFillDatas.size() != 0) {
            List<Map<String, Object>> list = new ArrayList();
            String type = "doc";
            String docIdField = "doc_id";
            String routingField = "_routing";
            String regNoField = "regno";
            String elasticRowField = "row_field";
            Map<String, List<ElasticCRFFillData>> elasticCRFFillDataMap = (Map)elasticCRFFillDatas.stream().collect(Collectors.groupingBy(ElasticCRFFillData::getDocId));
            Set<String> docIds = elasticCRFFillDataMap.keySet();
            IdsQueryBuilder idsQueryBuilder = QueryBuilders.idsQuery(new String[]{type}).addIds((String[])docIds.toArray(new String[0]));
            SearchSourceBuilder searchSourceBuilder = (new SearchSourceBuilder()).query(idsQueryBuilder);
            SearchRequest searchRequest = (new SearchRequest(new String[]{index})).source(searchSourceBuilder);
            List<Map<String, Object>> result = ElasticManager.query().scrollSearch(searchRequest);
            Map<String, List<Map<String, Object>>> resultMap = null;
            if (result != null && result.size() > 0) {
                resultMap = (Map)result.stream().collect(Collectors.groupingBy((obj) -> {
                    return obj.getOrDefault("doc_id", "").toString();
                }));
            } else {
                resultMap = new HashMap();
            }

            Iterator var16 = elasticCRFFillDataMap.entrySet().iterator();

            while(true) {
                String docId;
                List curElasticCRFFillDatas;
                do {
                    do {
                        if (!var16.hasNext()) {
                            if (list.size() > 0) {
                                try {
                                    ElasticManager.index().refreshIndex(new String[]{index});
                                } catch (IOException var29) {
                                    var29.printStackTrace();
                                }

                                this.bulk(index, list);
                            }

                            return;
                        }

                        Map.Entry<String, List<ElasticCRFFillData>> entry = (Map.Entry)var16.next();
                        docId = (String)entry.getKey();
                        System.out.println("ES表单数据文档id: " + docId);
                        curElasticCRFFillDatas = (List)entry.getValue();
                    } while(curElasticCRFFillDatas == null);
                } while(curElasticCRFFillDatas.size() == 0);

                List<Map<String, Object>> curResults = (List)((Map)resultMap).get(docId);
                Map<String, Object> curResult = null;
                if (curResults != null && curResults.size() > 0) {
                    curResult = (Map)curResults.get(0);
                }

                String regNo = "";
                Iterator var23 = curElasticCRFFillDatas.iterator();

                while(var23.hasNext()) {
                    ElasticCRFFillData elasticCRFFillData = (ElasticCRFFillData)var23.next();
                    Map<String, Object> fillData = elasticCRFFillData.getFillData();
                    String fieldName = elasticCRFFillData.getFieldName();
                    regNo = StringUtil.isNotEmpty(regNo) ? regNo : fillData.getOrDefault(regNoField, "").toString();
                    JSONObject crfFillDataObj = (JSONObject)fillData.get(fieldName);
                    if (crfFillDataObj != null) {
                        String rowField = crfFillDataObj.getString(elasticRowField);
                        if (curResult == null) {
                            fillData.put(docIdField, docId);
                            fillData.put(routingField, regNo);
                            curResult = fillData;
                        } else {
                            curResult = this.getCRFTableData(curResult, fieldName, rowField, crfFillDataObj);
                        }
                    }
                }

                if (StringUtil.isNotEmpty(regNo)) {
                    curResult.put(routingField, regNo);
                    System.out.println("同步表单填写数据至es： " + curResult.toString());
                    list.add(curResult);
                }
            }
        }
    }

    private Map<String, Object> getCRFTableData(Map<String, Object> result, String fieldName, String rowField, final JSONObject crfFillDataObj) {
        if (result != null && result.size() != 0) {
            if (StringUtil.isEmpty(rowField)) {
                result.put(fieldName, crfFillDataObj);
                return result;
            } else {
                Object crfFillData = result.get(fieldName);
                if (crfFillData == null) {
                    result.put(fieldName, crfFillDataObj);
                    return result;
                } else {
                    if (crfFillData instanceof List) {
                        boolean flag = false;
                        List<Map<String, Object>> crfFillDataList = (List)crfFillData;
                        Iterator var8 = crfFillDataList.iterator();

                        while(var8.hasNext()) {
                            Map<String, Object> crfFillDataMap = (Map)var8.next();
                            String curRowField = crfFillDataMap.getOrDefault("row_field", "").toString();
                            if (StringUtil.isNotEmpty(curRowField) && curRowField.equals(rowField)) {
                                crfFillDataList.remove(crfFillDataMap);
                                crfFillDataList.add(crfFillDataObj);
                                flag = true;
                                break;
                            }
                        }

                        if (!flag) {
                            crfFillDataList.add(crfFillDataObj);
                        }
                    } else if (crfFillData instanceof Map) {
                        final Map<String, Object> crfFillDataMap = (Map)crfFillData;
                        String curRowField = crfFillDataMap.getOrDefault("row_field", "").toString();
                        if (StringUtil.isNotEmpty(curRowField) && curRowField.equals(rowField)) {
                            result.put(fieldName, crfFillDataObj);
                        } else {
                            Object crfFillData = new ArrayList<Object>() {
                                {
                                    this.add(crfFillDataMap);
                                    this.add(crfFillDataObj);
                                }
                            };
                            result.put(fieldName, crfFillData);
                        }
                    }

                    return result;
                }
            }
        } else {
            return result;
        }
    }
}
