package com.learning.es.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.learning.core.exception.SpringBootException;
import com.learning.es.bean.IndicesStatusLocal;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.enums.ESFieldTypeEnum;
import com.learning.es.model.ConfigProperties;
import com.learning.es.model.elastic.ElasticFieldType;
import com.learning.es.model.MappingPropertiesModel;
import com.learning.es.model.SettingModel;
import com.learning.es.model.elastic.*;
import com.learning.es.service.IndexService;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.*;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.*;

@Log4j2
public class IndexServiceImpl
        implements IndexService {

    protected RestHighLevelClient client;
    protected RestClient restClient;

    /**
     * 通过es链接工厂类创建索引服务类
     * @param restClientFactory es链接工厂类
     */
    public IndexServiceImpl(RestClientFactory restClientFactory) {
        if (restClientFactory == null) {
            throw new SpringBootException("ES RestClient 为空， 请检查ES连接");
        } else {
            this.client = restClientFactory.getRestHighLevelClient();
            this.restClient = restClientFactory.getRestClient();
        }
    }

    /**
     * 刷新索引
     * @param indices
     * @return
     * @throws IOException
     */
    public String refreshIndex(String... indices)
            throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        for(String index : indices) {
            stringBuilder.append(index).append(",");
        }

        //去除字符串尾部最后一个","
        String str = stringBuilder.substring(0, stringBuilder.length() - 1);
        String search = "/" + str + "/_refresh";
        Request request = new Request("POST", search);
        Response response = this.restClient.performRequest(request);

        //将查询结果转化为字符串
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    /**
     * 获取es索引相应的设置
     * @param indices 索引
     * @return
     * @throws IOException
     */
    public String getSetting(String... indices)
            throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        for(String index : indices) {
            stringBuilder.append(index).append(",");
        }

        //去除字符串尾部最后一个","
        String str = stringBuilder.substring(0, stringBuilder.length() - 1);
        String search = "/" + str + "/_settings?format=json&pretty";
        Request request = new Request("GET", search);
        Response response = this.restClient.performRequest(request);

        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    /**
     * 获取索引中的字段
     * @param indices 索引
     * @return
     * @throws IOException
     */
    public String getMapping(String... indices) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        for(String index : indices) {
            stringBuilder.append(index).append(",");
        }

        //去除字符串尾部最后一个","
        String str = stringBuilder.substring(0, stringBuilder.length() - 1);
        String search = "/" + str + "/_mapping";
        Request request = new Request("GET", search);
        Response response = this.restClient.performRequest(request);

        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    /**
     * 刷新索引设置
     * @param settingsMap key为索引设置名称，value表示新值
     * @param indices 索引
     * @return 是否刷新成功
     */
    public Boolean updateSettings(Map<String, Object> settingsMap, String... indices) {
        UpdateSettingsRequest request = new UpdateSettingsRequest(indices);
        request.settings(settingsMap);

        try {
            AcknowledgedResponse response = this
                    .client
                    .indices()
                    .putSettings(request, RequestOptions.DEFAULT);
            return response.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 为索引添加字段
     * @param index 索引
     * @param elasticFieldTypes
     * @return
     */
    public Boolean addField(String index, ElasticFieldType... elasticFieldTypes) {
        String responseBody = "";
        String endPoint = index + "/_mapping/" + "doc";

        try {
            XContentBuilder mapping = this.getFieldMapping(elasticFieldTypes);
            String mappingStr = Strings.toString(mapping);
            Request request = new Request("PUT", endPoint);
            request.setEntity(new NStringEntity(mappingStr, ContentType.APPLICATION_JSON));
            Response response = this.restClient.performRequest(request);
            responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return JSONObject.parseObject(responseBody).getBoolean("acknowledged");
    }

    /**
     * 判断索引是否存在
     * @param indices 索引数组
     * @return
     */
    public boolean indexExists(String... indices) {
        boolean ret = false;
        StringBuilder stringBuilder = new StringBuilder();

        for(String index : indices) {
            stringBuilder.append(index).append(",");
        }

        String str = stringBuilder.toString().substring(0, stringBuilder.length() - 1);
        String search = "/" + str;
        Request request = new Request("head", search);

        try {
            Response response = this.restClient.performRequest(request);
            ret = response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            log.error("es error:", e);
        }

        return ret;
    }

    /**
     * 创建索引
     * @param index 索引
     * @param setting 索引设置
     * @param mapping 字段
     * @return
     */
    public boolean createIndex(String index, Object setting, Object mapping) {
        boolean flag = true;

        if (this.indexExists(index)) {
            return true;
        } else {
            String responseBody = "";

            try {
                //存储字段与索引map对象
                Map<String, Object> map = new HashMap<>();

                if (setting != null) {
                    map.put("settings", setting);
                }

                if (mapping != null) {
                    map.put("mappings", mapping);
                }

                String jsonString = JSON.toJSONString(map);
                Request request = new Request("PUT", index);
                request.setEntity(new NStringEntity(jsonString, ContentType.APPLICATION_JSON));
                Response response = this.restClient.performRequest(request);
                responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                log.error("ES创建索引错误,", e);
            }

            JSONObject jsonObject = JSONObject.parseObject(responseBody);

            return jsonObject.getBoolean("acknowledged");
        }
    }

    /**
     * 创建索引
     * @param index 索引
     * @param setting 索引设置
     * @param properties 字段设置
     * @return
     */
    public boolean createIndex(String index, SettingModel setting, MappingPropertiesModel properties) {
        boolean flag = true;
        if (this.indexExists(index)) {
            return true;
        } else {
            String responseBody = "";
            String mappingJson = "";

            try {
                XContentBuilder mapping = this.buildIndexMapping(properties);
                //获取索引设置json字符串
                String settingJson = this.buildSetting(setting)
                        .build()
                        .toString();

                String mappingStr = Strings.toString(mapping);
                //设置字段json字符串
                if (StringUtils.isNotEmpty(mappingStr)) {
                    mappingJson = ",\"mappings\":{\"doc\":" + mappingStr + "}";
                }

                Request request = new Request("PUT", index);
                request.setEntity(new NStringEntity("{\"settings\":" + settingJson + mappingJson + "}", ContentType.APPLICATION_JSON));
                Response response = this.restClient.performRequest(request);
                responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                log.error("ES创建索引错误,", e);
            }

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            return jsonObject.getBoolean("acknowledged");
        }
    }

    /**
     * 构建监视器索引映射
     * @param index 索引
     * @param setting 索引设置
     * @return
     */
    public boolean createMonitorIndex(String index, SettingModel setting) {
        boolean flag = true;
        if (this.indexExists(index)) {
            log.info("es-monitor 索引已经存在");
            return true;
        } else {
            log.info("创建es-monitor索引");
            String responseBody = "";
            String mappingJson = "";

            try {
                XContentBuilder mapping = this.buildMonitorIndexMapping();
                String settingJson = this.buildSetting(setting).build().toString();
                String mappingStr = mapping.getOutputStream().toString();
                if (StringUtils.isNotEmpty(mappingStr)) {
                    mappingJson = ",\"mappings\":{\"doc\":" + mappingStr.substring(0, mappingStr.length() - 2) + "}}}";
                }

                String jsonString = "{\"settings\":" + settingJson + mappingJson + "}";
                Request request = new Request("PUT", index);
                request.setEntity(new NStringEntity(jsonString, ContentType.APPLICATION_JSON));
                Response response = this.restClient.performRequest(request);
                responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            } catch (IOException e) {
                log.error("创建es-monitor索引失败", e);
            }

            JSONObject jsonObject = JSONObject.parseObject(responseBody);
            if (jsonObject != null) {
                flag = jsonObject.getBoolean("acknowledged");
            } else {
                flag = false;
            }

            return flag;
        }
    }

    /**
     * 删除索引
     * @param indices 索引数组
     * @return
     */
    public boolean deleteIndex(String... indices) {
        DeleteIndexRequest request = new DeleteIndexRequest(indices);

        try {
            AcknowledgedResponse deleteResponse = this
                    .client
                    .indices()
                    .delete(request, RequestOptions.DEFAULT);
            return deleteResponse.isAcknowledged();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取索引
     * @param indexName 索引名称
     * @return
     * @throws IOException
     */
    public List<IndicesStatusLocal> getIndices(String indexName) throws IOException {
        List<IndicesStatusLocal> indicesStatLocals = new ArrayList<>();
        //拼接查询字符串
        String search = "/_cat/indices/";

        if (StringUtils.isNotEmpty(indexName)) {
            search = search + indexName;
        }

        search = search + "?v&format=json&pretty";
        //设置请求
        Request request = new Request("GET", search);
        Response response = this.restClient.performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        String k = "[ ]";
        String line = "\n";
        //结果非空
        if (!k.equals(responseBody.replace(line, ""))) {
            JSONArray jsonArray = JSONArray.parseArray(responseBody);

            for (int i = 0; i < jsonArray.size(); ++i) {
                IndicesStatusLocal indicesStatusLocal = new IndicesStatusLocal();
                indicesStatusLocal.setUuid(jsonArray.getJSONObject(i).getString("uuid"));
                indicesStatusLocal.setIndexName(jsonArray.getJSONObject(i).getString("index"));
                indicesStatusLocal.setPrimary(jsonArray.getJSONObject(i).getInteger("pri"));
                indicesStatusLocal.setReplicas(jsonArray.getJSONObject(i).getInteger("rep"));
                indicesStatusLocal.setCountDocs(jsonArray.getJSONObject(i).getLong("docs.count"));
                indicesStatusLocal.setDeletedDocs(jsonArray.getJSONObject(i).getLong("docs.deleted"));
                indicesStatusLocal.setTotalStoreSize(jsonArray.getJSONObject(i).getString("store.size"));
                indicesStatusLocal.setPrimaryStoreSize(jsonArray.getJSONObject(i).getString("pri.store.size"));
                indicesStatusLocal.setHealth(jsonArray.getJSONObject(i).getString("health"));
                indicesStatLocals.add(indicesStatusLocal);
            }

        }
        return indicesStatLocals;
    }

    /**
     * 获取分片状态
     * @return
     */
    public String getShardStats() {
        String responseBody = "";
        String search = "/_cat/shards/?v&format=json&pretty";
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
     * 构建动态模板映射
     * @return
     * @throws IOException
     */
    public XContentBuilder buildDynamicTemplatesMapping() throws IOException {
        XContentBuilder builder = XContentFactory
                .jsonBuilder()
                .startObject()
                .startArray("dynamic_templates")
                .startObject()
                .startObject("pattern_as_keywords")
                .field("match", "^(?!object_|array_|select_|radio_|checkbox_).*")
                .field("match_pattern", "regex")
                .startObject("mapping")
                .field("type", "keyword")
                .startObject("fields")
                .startObject("keyword")
                .field("type", "keyword")
                .field("ignore_above", "256")
                .endObject()
                .endObject()
                .endObject()
                .endObject()
                .endObject()
                .endArray()
                .endObject();
        log.info(Strings.toString(builder));

        return builder;
    }

    /**
     * 构建索引设置构造器
     * @param setting 索引设置
     * @return
     */
    private Settings.Builder buildSetting(SettingModel setting) {
        return Settings
                .builder()
                .put("index.number_of_shards", setting.getShards())
                .put("index.number_of_replicas", setting.getReplicas());
    }

    /**
     * 构造属性构造器
     * @param properties 属性
     * @return
     * @throws IOException
     */
    private XContentBuilder buildIndexMapping(MappingPropertiesModel properties)
            throws IOException {
        XContentBuilder builder = XContentFactory
                .jsonBuilder()
                .startObject()
                .startObject("_routing")
                .field("required", true)
                .endObject()
                .startObject("properties");
        JoinFieldMetadata joinFieldMetadata = properties.getJoinFieldMetadata();
        List<BaseFieldMetadata> metadata = properties.getMetadata();

        //构造映射元数据
        if (joinFieldMetadata != null) {
            builder.startObject(joinFieldMetadata.getJoinField());
            builder.field("type", joinFieldMetadata.getType());
            builder.field("eager_global_ordinals", joinFieldMetadata.isEagerGlobalOrdinals());
            //获取元素之间的关系
            Map<String, ArrayList<String>> relations = joinFieldMetadata.getRelations();
            if (relations != null && relations.size() > 0) {
                builder.startObject("relations");

                //拼接元素关系
                for (Map.Entry<String, ArrayList<String>> entry : relations.entrySet()) {
                    String key = entry.getKey();
                    ArrayList<String> value = entry.getValue();
                    if (value.size() == 1) {
                        builder.field(key, value.get(0));
                    } else if (value.size() > 1) {
                        builder.field(key, value);
                    }
                }

                builder.endObject();
            }

            builder.endObject();
        }

        Iterator it = metadata.iterator();

        //拼接字段
        while(true) {
            while(true) {
                BaseFieldMetadata base;
                String curType;
                do {
                    if ( !it.hasNext()) {
                        DateFieldMetadata dateFieldMetadata = new DateFieldMetadata("timestamp");
                        builder.startObject("regno")
                                .field("type", "keyword")
                                .endObject()
                                .startObject("admno")
                                .field("type", "keyword")
                                .endObject()
                                .startObject("doc_id")
                                .field("type", "keyword")
                                .endObject()
                                .startObject("source_id")
                                .field("type", "keyword")
                                .endObject()
                                .startObject("order")
                                .field("type", "long")
                                .endObject()
                                .startObject(dateFieldMetadata.getName())
                                .field("type", dateFieldMetadata.getType())
                                .field("format", dateFieldMetadata.getFormat())
                                .field("ignore_malformed", true)
                                .endObject()
                                .endObject()
                                .endObject();
                        return builder;
                    }

                    base = (BaseFieldMetadata)it.next();
                    curType = base.getType();
                } while(StringUtils.isEmpty(curType));

                //设置相应的字段类型
                curType = curType.toLowerCase();
                byte code = -1;
                switch(curType.hashCode()) {
                    case -814408215:
                        if (curType.equals("keyword")) {
                            code = 1;
                        }
                        break;
                    case 104431:
                        if (curType.equals("int")) {
                            code = 6;
                        }
                        break;
                    case 3076014:
                        if (curType.equals("date")) {
                            code = 2;
                        }
                        break;
                    case 3556653:
                        if (curType.equals("text")) {
                            code = 0;
                        }
                        break;
                    case 3560141:
                        if (curType.equals("time")) {
                            code = 4;
                        }
                        break;
                    case 1793702779:
                        if (curType.equals("datetime")) {
                            code = 3;
                        }
                        break;
                    case 1958052158:
                        if (curType.equals("integer")) {
                            code = 5;
                        }
                }

                //设置对应的字段属性
                switch(code) {
                    case 0:
                        TextFieldMetadata textFieldMetadata = (TextFieldMetadata)base;
                        builder.startObject(textFieldMetadata.getName())
                               .field("type", textFieldMetadata.getType())
                               .field("analyzer", textFieldMetadata.getAnalyzer())
                               .field("search_analyzer", textFieldMetadata.getSearchAnalyzer())
                               .field("index_options", "offsets");
                        if (textFieldMetadata.getCreateKeyword() || textFieldMetadata.getName().endsWith("name") || textFieldMetadata.getName().endsWith("dept") || textFieldMetadata.getName().equals(ConfigProperties.getKey("es.query.field.lisitem.format"))) {
                            builder.startObject("fields")
                                   .startObject("keyword")
                                   .field("type", "keyword")
                                   .endObject()
                                   .endObject();
                        }

                        builder.endObject();
                        break;
                    case 1:
                        KeywordFieldMetadata keywordFieldMetadata = (KeywordFieldMetadata)base;
                        builder.startObject(keywordFieldMetadata.getName())
                                .field("type", keywordFieldMetadata.getType());
                        if (keywordFieldMetadata.getName().endsWith("name") || keywordFieldMetadata.getName().endsWith("dept") || keywordFieldMetadata.getName().equals(ConfigProperties.getKey("es.query.field.lisitem.format"))) {
                            builder.startObject("fields")
                                   .startObject("keyword")
                                   .field("type", "keyword")
                                   .endObject()
                                   .endObject();
                        }

                        builder.endObject();
                        break;
                    case 2:
                    case 3:
                    case 4:
                        DateFieldMetadata dateFieldMetadata = (DateFieldMetadata)base;
                        builder.startObject(dateFieldMetadata.getName());
                        builder.field("type", dateFieldMetadata.getType());
                        builder.field("format", dateFieldMetadata.getFormat());
                        builder.field("ignore_malformed", true);
                        builder.endObject();
                        break;
                    case 5:
                    case 6:
                        NumberFieldMetadata integetFieldMetadata = (NumberFieldMetadata)base;
                        builder.startObject(integetFieldMetadata.getName());
                        builder.field("type", integetFieldMetadata.getType());
                        builder.field("ignore_malformed", true);
                        builder.endObject();
                }
            }
        }
    }

    /**
     * 构建监视器索引映射
     * @return
     * @throws IOException
     */
    private XContentBuilder buildMonitorIndexMapping()
            throws IOException {
        return XContentFactory
                .jsonBuilder()
                .startObject()
                .field("dynamic", false)
                .field("date_detection", false)
                .startObject("properties")
                .startObject("cluster_state")
                .field("type", "object")
                .endObject().startObject("cluster_stats")
                .field("type", "object")
                .endObject()
                .startObject("cluster_uuid")
                .field("type", "keyword")
                .endObject()
                .startObject("index_recovery")
                .field("type", "object")
                .endObject().startObject("index_stats")
                .field("type", "object")
                .endObject()
                .startObject("indices_stats")
                .field("type", "object")
                .endObject()
                .startObject("interval_ms")
                .field("type", "long")
                .endObject()
                .startObject("job_stats")
                .field("type", "object")
                .endObject()
                .startObject("node_stats")
                .field("type", "object")
                .endObject()
                .startObject("shard")
                .field("type", "object")
                .endObject()
                .startObject("source_node")
                .field("type", "object")
                .endObject()
                .startObject("state_uuid")
                .field("type", "keyword")
                .endObject()
                .startObject("timestamp")
                .field("type", "long")
                .endObject()
                .startObject("type")
                .field("type", "keyword")
                .endObject()
                .endObject()
                .endObject();
    }

    /**
     * 构造构造index的mappings结构
     * @param elasticFieldTypes
     * @return
     * @throws IOException
     */
    private XContentBuilder getFieldMapping(ElasticFieldType... elasticFieldTypes) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder().startObject();
        builder = this.buildFieldMapping(builder, elasticFieldTypes);
        builder.endObject();
        return builder;
    }

    /**
     * 构造构造index的mappings结构
     * @param builder 构造器
     * @param elasticFieldTypes ES字段名称及类型
     * @return
     * @throws IOException
     */
    private XContentBuilder buildFieldMapping(XContentBuilder builder, ElasticFieldType... elasticFieldTypes) throws IOException {
        if (elasticFieldTypes != null && elasticFieldTypes.length != 0) {
            //设置json开头
            builder.startObject("properties");

            for(ElasticFieldType elasticFieldType : elasticFieldTypes) {

                if (StringUtils.isEmpty(elasticFieldType.getFieldName()) || StringUtils.isEmpty(elasticFieldType.getFieldType())) {
                    return null;
                }

                //获取嵌套ES字段名称及类型
                List<ElasticFieldType> child = elasticFieldType.getChild();
                //获取字段类型
                String fieldType = elasticFieldType.getFieldType().toLowerCase();
                //获取字段名称
                String fieldName = elasticFieldType.getFieldName();
                //获取字段类型对应的枚举
                ESFieldTypeEnum fieldTypeEnum = ESFieldTypeEnum
                        .enumMap
                        .getOrDefault(fieldType, null);

                //根据字段类型枚举设置相应的json格式
                if (fieldTypeEnum != null) {

                    switch(fieldTypeEnum) {
                        case TEXT:
                            TextFieldMetadata textFieldMetadata = new TextFieldMetadata(fieldName);
                            builder.startObject(textFieldMetadata.getName());
                            builder.field("type", textFieldMetadata.getType());
                            builder.field("analyzer", textFieldMetadata.getAnalyzer());
                            builder.field("search_analyzer", textFieldMetadata.getSearchAnalyzer());
                            builder.field("index_options", "offsets");
                            if (textFieldMetadata.getCreateKeyword() || textFieldMetadata.getName().equals(ConfigProperties.getKey("es.query.field.lisitem.format"))) {
                                builder.startObject("fields");
                                builder.startObject("keyword");
                                builder.field("type", "keyword");
                                builder.endObject();
                                builder.endObject();
                            }

                            builder.endObject();
                            break;
                        case KEYWORD:
                            KeywordFieldMetadata keywordFieldMetadata = new KeywordFieldMetadata(fieldName);
                            builder.startObject(keywordFieldMetadata.getName());
                            builder.field("type", keywordFieldMetadata.getType());
                            builder.endObject();
                            break;
                        case TIME:
                        case DATETIME:
                        case DATE:
                            DateFieldMetadata dateFieldMetadata = new DateFieldMetadata(fieldName);
                            builder.startObject(dateFieldMetadata.getName());
                            builder.field("type", dateFieldMetadata.getType());
                            builder.field("format", dateFieldMetadata.getFormat());
                            builder.field("ignore_malformed", true);
                            builder.endObject();
                            break;
                        case NUMBER:
                            NumberFieldMetadata integetFieldMetadata = new NumberFieldMetadata(fieldName);
                            builder.startObject(integetFieldMetadata.getName());
                            builder.field("type", integetFieldMetadata.getType());
                            builder.field("ignore_malformed", true);
                            builder.endObject();
                            break;
                        case OBJECT:
                            ObjectFieldMetadata objectFieldMetadata = new ObjectFieldMetadata(fieldName);
                            builder.startObject(objectFieldMetadata.getName());
                            builder.field("type", objectFieldMetadata.getType());
                            if (child != null && child.size() > 0) {
                                builder = this.buildFieldMapping(builder, child.toArray(new ElasticFieldType[0]));
                            }

                            builder.endObject();
                    }
                }
            }

            builder.endObject();
            return builder;
        } else {
            return null;
        }
    }
}
