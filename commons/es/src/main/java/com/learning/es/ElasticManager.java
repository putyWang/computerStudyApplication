package com.learning.es;

import com.learning.es.bean.Result;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.ConfigProperties;
import com.learning.es.service.*;
import com.learning.es.service.impl.*;

public final class ElasticManager {
    private static ElasticManager instance = null;
    private QueryService queryService;
    private IndexService indexService;
    private ClusterService clusterService;
    private SearchService searchService;
    private AggregationService aggregationService;
    private IDSLService dslService;
    private DocumentService documentService;

    public static ElasticManager getInstance() {
        if (instance == null) {
            Class var0 = ElasticManager.class;
            synchronized(ElasticManager.class) {
                if (instance == null) {
                    instance = new ElasticManager();
                }
            }
        }

        return instance;
    }

    public static void reset() {
        instance = null;
    }

    public static QueryService query() {
        return getInstance().queryService;
    }

    public static IndexService index() {
        return getInstance().indexService;
    }

    public static ClusterService cluster() {
        return getInstance().clusterService;
    }

    public static SearchService search() {
        return getInstance().searchService;
    }

    public static AggregationService aggregation() {
        return getInstance().aggregationService;
    }

    public static IDSLService dslService() {
        return getInstance().dslService;
    }

    public static DocumentService document() {
        return getInstance().documentService;
    }

    private ElasticManager() {
        this.serviceRegistry();
    }

    private void serviceRegistry() {
        RestClientFactory restClientFactory = RestClientFactory.getInstance(ElasticManager.class, ConfigProperties.getKey("es.network.address"));
        this.queryService = new QueryServiceImpl(restClientFactory);
        this.indexService = new IndexServiceImpl(restClientFactory);
        this.clusterService = new ClusterServiceImpl(restClientFactory);
        this.searchService = new SearchServiceImpl(restClientFactory);
        this.aggregationService = new AggregationServiceImpl(restClientFactory);
        this.dslService = new DSLServiceImpl(restClientFactory);
        this.documentService = new DocumentServiceImpl(restClientFactory);
    }
}
