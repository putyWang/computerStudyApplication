package com.learning.es.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.learning.core.exception.SpringBootException;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.service.ClusterService;
import com.learning.es.service.EsServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.client.*;

import java.io.IOException;
import java.util.Map;

@Log4j2
public class ClusterServiceImpl
        extends EsServiceImpl
        implements ClusterService {

    /**
     * 注入相应的es链接
     * @param restClientFactory es链接工厂类
     */
    public ClusterServiceImpl(RestClientFactory restClientFactory) {
        super(restClientFactory);
    }

    /**
     * 查询索引重建进度
     * @return
     */
    public Object getAllReindexTask() {
        String responseBody = "";
        String endPoint = "_tasks?detailed=true&actions=*reindex";

        try {
            Request request = new Request("GET", endPoint);
            Response response = this.restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSONObject.parseObject(responseBody);
    }

    /**
     * 通过taskId获取相应的task
     * @param taskId taskId
     * @return
     */
    public Object getReindexTaskById(String taskId) {
        String responseBody = "";
        String endPoint = "_tasks?" + taskId;

        try{
            Request request = new Request("GET", endPoint);
            Response response = this.restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSONObject.parseObject(responseBody);
    }

    /**
     * 更新集群设置
     * @param clusterMap 集群设置
     * @return
     */
    public Boolean updateSettings(Map<String, Object> clusterMap) {
        ClusterUpdateSettingsRequest request = new ClusterUpdateSettingsRequest();
        request.persistentSettings(clusterMap);

        try {
            return this
                    .client
                    .cluster()
                    .putSettings(request, RequestOptions.DEFAULT)
                    .isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取集群节点数据
     * @return
     */
    public String clusterNodes() {
        String responseBody = "";
        String search = "/_cat/nodes/?v&format=json&pretty";
        Request request = new Request("GET", search);

        try {
            Response response = this.restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseBody;
    }

    /**
     * 查询集群健康情况
     * @return
     */
    public ClusterHealthResponse clusterHealth() {
        ClusterHealthRequest request = new ClusterHealthRequest();

        try {
            return this.client.cluster().health(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 取消所有task
     * @return
     */
    public Object stopAllReindexTask() {
        String responseBody = "";
        String endPoint = "_tasks/_cancel?actions=*reindex";

        try {
            Request request = new Request("POST", endPoint);
            Response response = this.restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException var5) {
            var5.printStackTrace();
        }

        return JSONObject.parseObject(responseBody);
    }
}
