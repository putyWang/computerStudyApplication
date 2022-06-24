package com.learning.es.service;

import com.learning.es.bean.IndicesStatusLocal;
import com.learning.es.model.elastic.ElasticFieldType;
import com.learning.es.model.MappingPropertiesModel;
import com.learning.es.model.SettingModel;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * index索引相关服务类
 */
public interface IndexService {

    /**
     * 刷新索引
     * @param indices
     * @return
     * @throws IOException
     */
    String refreshIndex(String... indices)
            throws IOException;

    /**
     * 获取es索引相应的设置
     * @param indices 索引
     * @return
     * @throws IOException
     */
    String getSetting(String... indices)
            throws IOException;

    /**
     * 刷新索引设置
     * @param settingsMap key为索引设置名称，value表示新值
     * @param indices 索引
     * @return 是否刷新成功
     */
    Boolean updateSettings(Map<String, Object> settingsMap, String... indices);

    /**
     * 为索引添加字段
     * @param index 索引
     * @param elasticFieldTypes
     * @return
     */
    Boolean addField(String index, ElasticFieldType... elasticFieldTypes);

    String getMapping(String... var1) throws IOException;

    boolean indexExists(String... var1);

    boolean createIndex(String var1, Object var2, Object var3);

    boolean createIndex(String var1, SettingModel var2, MappingPropertiesModel var3);

    boolean createMonitorIndex(String var1, SettingModel var2);

    boolean deleteIndex(String... var1);

    List<IndicesStatusLocal> getIndices(String var1) throws IOException;

    String getShardStats();

    XContentBuilder buildDynamicTemplatesMapping() throws IOException;
}
