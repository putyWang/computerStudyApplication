package com.learning.es.service.impl;

import com.learning.es.FormElasticManager;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.ConfigProperties;
import com.learning.es.service.FormDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 表单es文档 service
 * @author ：wangpenghui
 * @date ：Created in 2021/3/10 11:17
 */
@Slf4j
public class FormDocumentServiceImpl implements FormDocumentService {


    /**
     * 批量同步数据至es
     * @param index es索引
     * @param docList es文档数据信息
     */
    @Override
    public void addBatch(String index, List<Map<String, Object>> docList) {
        if (CollectionUtils.isEmpty(docList)){
            return;
        }

        try {
            // 如果索引不存在，则新建索引
            if (!FormElasticManager.index().indexExists(index)){
                FormElasticManager.index().addIndex(index, new ElasticSetting());
            }

            // 刷新索引
            ElasticManager.index().refreshIndex(index);
            // 批量插入crf表单数据
            ElasticManager.document().bulk(index, docList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定id文档
     * @param index
     * @param id
     * @param routing
     * @return
     */
    @Override
    public boolean deleteById(String index, String id, String routing) {
        boolean delete = true;
        // 获取es高级客户端
        RestHighLevelClient client = RestClientFactory.getInstance(ElasticManager.class,
                ConfigProperties.getKey(ConfigConst.ES_NETWORK_ADDRESS)
        ).getRestHighLevelClient();

        DeleteRequest request = new DeleteRequest(
                index,
                ElasticConst.ELASTIC_DEFAULT_TYPE_NAME,
                id);
        request.routing(routing);

        try {
            DeleteResponse deleteResponse = client.delete(
                    request, RequestOptions.DEFAULT);
            ReplicationResponse.ShardInfo shardInfo = deleteResponse.getShardInfo();
            if (shardInfo.getFailed() > 0) {
                delete = false;
                for (ReplicationResponse.ShardInfo.Failure failure :
                        shardInfo.getFailures()) {
                    String reason = failure.reason();
                    log.info("删除es文档失败: " + reason);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return delete;
    }

    @Override
    public long deleteByIds(String index, List<String> ids) {
        long deletedDocs = 0;
        if (CollectionUtils.isEmpty(ids)){
            return deletedDocs;
        }

        // 获取es高级客户端
        RestHighLevelClient client = RestClientFactory.getInstance(ElasticManager.class,
                ConfigProperties.getKey(ConfigConst.ES_NETWORK_ADDRESS)
        ).getRestHighLevelClient();

        IdsQueryBuilder idsQueryBuilder = new IdsQueryBuilder();
        idsQueryBuilder.types(ElasticConst.ELASTIC_DEFAULT_TYPE_NAME);
        idsQueryBuilder.addIds(ids.toArray(new String[0]));
        DeleteByQueryRequest request = new DeleteByQueryRequest(index)
                .setQuery(idsQueryBuilder)
                .setRefresh(true)
                .setDocTypes(ElasticConst.ELASTIC_DEFAULT_TYPE_NAME);

        try {
            BulkByScrollResponse bulkResponse =
                    client.deleteByQuery(request, RequestOptions.DEFAULT);
            deletedDocs = bulkResponse.getDeleted();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deletedDocs;
    }

    @Override
    public long deleteByEmpis(String index, List<String> empis) {
        long deletedDocs = 0;
        if (CollectionUtils.isEmpty(empis)){
            return deletedDocs;
        }

        // 获取es高级客户端
        RestHighLevelClient client = RestClientFactory.getInstance(ElasticManager.class,
                ConfigProperties.getKey(ConfigConst.ES_NETWORK_ADDRESS)
        ).getRestHighLevelClient();

        QueryBuilder queryBuilder = QueryBuilders.termsQuery(ElasticConst.ELASTIC_FIELD_EMPI, empis);
        DeleteByQueryRequest request = new DeleteByQueryRequest(index)
                .setQuery(queryBuilder)
                .setRefresh(true)
                .setDocTypes(ElasticConst.ELASTIC_DEFAULT_TYPE_NAME);

        try {
            BulkByScrollResponse bulkResponse =
                    client.deleteByQuery(request, RequestOptions.DEFAULT);
            deletedDocs = bulkResponse.getDeleted();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return deletedDocs;
    }

}

