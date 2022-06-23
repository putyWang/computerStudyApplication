package com.learning.es.clients;

import com.learning.es.model.ConfigProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * rest链接工厂类
 */
public class RestClientFactory {
    private static final Logger log = LoggerFactory.getLogger(RestClientFactory.class);
    /**
     * 类是否被初始化
     */
    static volatile int INITIALIZATION_STATE = 0;
    private static final String SCHEMA = "http";
    private static final String CLUSTER_NODES_SPLIT_SYMBOL = ",";
    private static final String HOST_PORT_SPLIT_SYMBOL = ":";
    private static final int CONNECT_TIME_OUT = 1000;
    private static final int SOCKET_TIME_OUT = 30000;
    private static final int CONNECTION_REQUEST_TIME_OUT = 500;
    private static final int MAX_CONNECT_NUM = 100;
    /**
     * 设置每个路由值的最大连接数 默认值为500
     */
    private static final int MAX_CONNECT_PER_ROUTE = 500;
    private static final boolean uniqueConnectTimeConfig = true;
    private static final boolean uniqueConnectNumConfig = true;
    private static RestClientBuilder builder;
    private static final LinkedHashMap<String, RestClientFactory> matchingClassClients = new LinkedHashMap<>();
    private final List<HttpHost> HTTP_HOST = new ArrayList<>();
    private RestClient restClient;
    private RestHighLevelClient restHighLevelClient;

    /**
     * 添加所有es集群主机的http地址
     * @param httpHosts es集群主机地址
     */
    private RestClientFactory(List<HttpHost> httpHosts) {
        this.HTTP_HOST.addAll(httpHosts);
        this.init();
    }

    /**
     * 添加单机HttpHost地址
     * @param httpHost 单机es主机地址
     */
    private RestClientFactory(HttpHost httpHost) {
        this.HTTP_HOST.add(httpHost);
        this.init();
    }

    /**
     * 初始化es工厂类
     */
    private void init() {

        builder = RestClient.builder(this.HTTP_HOST.toArray(new HttpHost[0]));

        //是否设置超时
        if (uniqueConnectTimeConfig) {
            setConnectTimeOutConfig();
        }

        //是否设置链接相关超时
        if (uniqueConnectNumConfig) {
            setMutiConnectConfig();
        }

        this.restClient = builder.build();
        this.restHighLevelClient = new RestHighLevelClient(builder);

        //验证es链接
        try {
            boolean response = this.restHighLevelClient.ping(RequestOptions.DEFAULT);
            if (!response) {
                INITIALIZATION_STATE = 0;
                log.error("请检查ES连接");
            }
        } catch (IOException e) {
            INITIALIZATION_STATE = 0;
            log.error("请检查ES连接", e.getMessage());
        }

    }

    /**
     * 获取clazz对应的es链接
     * @param clazz es对应的key
     * @return
     */
    public static RestClientFactory getInstance(Class<?> clazz) {
        return getInstance(clazz, ConfigProperties.getKey("es.network.address"));
    }

    /**
     * 根据networkAddress字符串获取clazz对应的工厂对象
     * @param clazz es对应的key
     * @param networkAddress 输入的es ip地址
     * @return
     */
    public static RestClientFactory getInstance(Class<?> clazz, String networkAddress) {
        String[] clusterNodeArray = networkAddress.trim().split(CLUSTER_NODES_SPLIT_SYMBOL);
        List<HttpHost> httpHosts = new ArrayList<>();

        //添加相关es的ip地址
        for(String address :  clusterNodeArray) {
            String[] clusterNodeInfoArray = address.trim().split(HOST_PORT_SPLIT_SYMBOL);
            HttpHost httpHost = new HttpHost(clusterNodeInfoArray[0], Integer.parseInt(clusterNodeInfoArray[1]), "http");
            httpHosts.add(httpHost);
        }

        return getInstance(clazz.getName(), httpHosts);
    }

    /**
     * 通过es ip 数组获取es链接工厂
     * @param clazz es对应的key
     * @param httpHosts es链接数组
     * @return
     */
    public static RestClientFactory getInstance(String clazz, List<HttpHost> httpHosts) {
        if (INITIALIZATION_STATE == 0) {
            //若http未存在时，初始化，存在时共享
            synchronized(RestClientFactory.class) {
                if (INITIALIZATION_STATE == 0) {
                    INITIALIZATION_STATE = 1;
                    RestClientFactory factory = new RestClientFactory(httpHosts);
                    matchingClassClients.put(clazz, factory);
                    return factory;
                }
            }
        }

        return matchingClassClients.get(clazz);
    }

    /**
     * 通过单一es ip 获取es链接工厂
     * @param clazz es对应的key
     * @param httpHost es链接
     * @return
     */
    public static RestClientFactory getInstance(String clazz, HttpHost httpHost) {

        if (INITIALIZATION_STATE == 0) {
            synchronized(RestClientFactory.class) {
                if (INITIALIZATION_STATE == 0) {
                    INITIALIZATION_STATE = 1;
                    RestClientFactory factory = new RestClientFactory(httpHost);
                    matchingClassClients.put(clazz, factory);
                    return factory;
                }
            }
        }

        return matchingClassClients.get(clazz);
    }

    public RestClient getRestClient() {
        return this.restClient;
    }

    public RestHighLevelClient getRestHighLevelClient() {
        return this.restHighLevelClient;
    }

    /**
     * 设置链接超时时间
     */
    public static void setConnectTimeOutConfig() {
        builder.setRequestConfigCallback((requestConfigBuilder) -> {
            //设置链接超时
            requestConfigBuilder.setConnectTimeout(CONNECT_TIME_OUT);
            //设置套接字连接超时
            requestConfigBuilder.setSocketTimeout(SOCKET_TIME_OUT);
            //设置链接请求超时
            requestConfigBuilder.setConnectionRequestTimeout(CONNECTION_REQUEST_TIME_OUT);
            return requestConfigBuilder;
        });
    }

    /**
     * 设置链接凭证
     */
    public static void setMutiConnectConfig() {

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

        //设置需要凭证范围及验证凭证账户密码
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic-mrp", "dhcc-mrp"));

        builder.setHttpClientConfigCallback((httpClientBuilder) -> {
            //设置最大连接数
            httpClientBuilder.setMaxConnTotal(MAX_CONNECT_NUM);
            //设置每个路由值的最大连接数
            httpClientBuilder.setMaxConnPerRoute(MAX_CONNECT_PER_ROUTE);
            //禁用身份验证缓存
            httpClientBuilder.disableAuthCaching();
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            return httpClientBuilder;
        });
    }

    /**
     * 重置工厂类
     */
    public static void reset() {
        INITIALIZATION_STATE = 0;
    }

    /**
     * 根据clazz类获取相应的restClient
     * @param clazz
     * @param host
     * @param port
     * @return
     */
    public static RestClient getRestClient(Class<?> clazz, String host, int port) {
        return getRestClient(clazz.getName(), host, port);
    }

    /**
     * 根据clazz类名获取相应的restClient
     * @param className
     * @param host
     * @param port
     * @return
     */
    public static RestClient getRestClient(String className, String host, int port) {
        HttpHost httpHost = new HttpHost(host, port, SCHEMA);
        RestClientFactory factory = getInstance(className, httpHost);
        return factory.restClient;
    }

    /**
     * 使用clazz相应的RestHighLevelClient
     * @param clazz
     * @param networkAddress
     * @return
     */
    public static RestHighLevelClient getRestHighLevelClient(Class<?> clazz, String networkAddress) {
        String[] clusterNodeArray = networkAddress.trim().split(CLUSTER_NODES_SPLIT_SYMBOL);
        List<HttpHost> httpHosts = new ArrayList<>();

        //根据netwoekAddress获取es链接数组
        for(String address : clusterNodeArray) {
            String[] clusterNodeInfoArray = address.trim().split(HOST_PORT_SPLIT_SYMBOL);
            HttpHost httpHost = new HttpHost(clusterNodeInfoArray[0], Integer.parseInt(clusterNodeInfoArray[1]), SCHEMA);
            httpHosts.add(httpHost);
        }

        RestClientFactory factory = getInstance(clazz.getName(), httpHosts);
        return factory.restHighLevelClient;
    }

    public static RestHighLevelClient getRestHighLevelClient(String className, String host, int port) {
        HttpHost httpHost = new HttpHost(host, port, "http");
        RestClientFactory factory = getInstance(className, httpHost);
        return factory.restHighLevelClient;
    }

    public void close() {
        if (this.restClient != null) {
            try {
                this.restClient.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

    }
}
