package com.learning.es.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.learning.core.utils.ArrayUtils;
import com.learning.core.utils.DateTimeUtil;
import com.learning.core.utils.StringUtils;
import com.learning.es.ElasticManager;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.condition.ConditionBuilder;
import com.learning.es.service.DocumentService;
import com.learning.es.service.EsServiceImpl;
import lombok.extern.log4j.Log4j2;
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

    public void updateByDocId(String index, String docId, String routing, String jsonString) {
        if (! StringUtils.isEmpty(jsonString)) {
            String type = "doc";
            //设置更新请求
            UpdateRequest request = new UpdateRequest()
                    .index(index)
                    .id(docId)
                    .routing(routing)
                    .type(type)
                    .doc(jsonString, XContentType.JSON)
                    .docAsUpsert(true);

            //执行更新请求
            executeUpdate(request);

        }
    }


    public void updateByDocId(String index, String docId, String routing, Map<String, Object> data) {
        String type = "doc";
        UpdateRequest request = (new UpdateRequest())
                .index(index)
                .id(docId)
                .routing(routing)
                .type(type)
                .doc(data)
                .docAsUpsert(true);

        executeUpdate(request);
    }

    /**
     * 执行更新请求
     * @param request 更新请求
     */
    private void executeUpdate (UpdateRequest request) {
        try {
            UpdateResponse updateResponse = this
                    .client
                    .update(request, RequestOptions.DEFAULT);

            //输出相应的日志
            if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
                log.info("elastic文档： " + updateResponse.getId() + "，新增成功");
            } else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                log.info("elastic文档： " + updateResponse.getId() + "，数据修改成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
