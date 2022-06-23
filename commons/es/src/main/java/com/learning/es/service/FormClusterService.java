package com.learning.es.service;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;

/**
 * @author wangpenghui
 * @createTime 2021年08月17日 11:56:00
 */
public interface FormClusterService {

    ClusterHealthResponse clusterHealth();
}
