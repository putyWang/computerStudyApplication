package com.learning.es.service.impl;

import com.learning.es.ElasticManager;
import com.learning.es.service.FormClusterService;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;

/**
 * @author wangpenghui
 * @createTime 2021年08月17日 11:57:00
 */
public class FormClusterServiceImpl implements FormClusterService {

    @Override
    public ClusterHealthResponse clusterHealth() {
        return ElasticManager.cluster().clusterHealth();
    }

}
