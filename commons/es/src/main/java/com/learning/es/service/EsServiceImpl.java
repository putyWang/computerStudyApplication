package com.learning.es.service;

import com.learning.core.exception.ElasticException;
import com.learning.core.exception.SpringBootException;
import com.learning.es.clients.RestClientFactory;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EsServiceImpl {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    protected RestHighLevelClient client;
    protected RestClient restClient;

    public EsServiceImpl(RestClientFactory restClientFactory) {

        if (restClientFactory == null) {
            throw new ElasticException("ES RestClient 为空， 请检查ES连接");
        } else {
            this.client = restClientFactory.getRestHighLevelClient();
            this.restClient = restClientFactory.getRestClient();
        }
    }
}
