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

    String refreshIndex(String... var1) throws IOException;

    String getSetting(String... var1) throws IOException;

    Boolean updateSettings(Map<String, Object> var1, String... var2);

    Boolean addField(String var1, ElasticFieldType... var2);

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
