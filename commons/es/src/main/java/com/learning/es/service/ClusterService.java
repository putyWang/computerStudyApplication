package com.learning.es.service;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;

import java.util.Map;

/**
 * 集群相关服务类
 */
public interface ClusterService {

    Object getAllReindexTask();

    Object getReindexTaskById(String taskId);

    Boolean updateSettings(Map<String, Object> clusterMap);

    String clusterNodes();

    ClusterHealthResponse clusterHealth();

    Object stopAllReindexTask();
}
