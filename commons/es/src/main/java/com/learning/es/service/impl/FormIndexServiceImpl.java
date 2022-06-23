package com.learning.es.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.boot.commons.exception.BusinessException;
import com.boot.form.query.constants.ElasticConst;
import com.boot.form.query.model.ElasticSetting;
import com.boot.form.query.model.elastic.DateFieldMetadata;
import com.boot.form.query.service.FormIndexService;
import com.dhcc.mrp.common.models.elastic.ElasticFieldType;
import com.dhcc.mrp.elastic.ElasticManager;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.*;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * 表单索引 service
 * @author ：wangpenghui
 * @date ：Created in 2021/3/15 10:52
 */
public class FormIndexServiceImpl implements FormIndexService {

//    private static final String ELASTIC_OBJECT_FIELD_REGEX = "^(?:object_|array_|select_|radio_|checkbox_|autocomplete_|groups_|t_nation_).*";
    private static final String ELASTIC_OBJECT_FIELD_REGEX = "^(?:object_|array_|select_|checkbox_|autocomplete_|groups_|t_nation_).*";

    private static final String ELASTIC_KEYWORD_FIELD_REGEX = "^(?:input_|inputnumber_|textarea_|score_).*";

    /**
     * 获取es索引名称
     *
     * @param subProject
     * @return
     */
    @Override
    public String getIndexName(Long subProject) {
        String indexName = ElasticConst.ELASTIC_DEFAULT_INDEX_NAME;
        if (subProject != null && subProject != 0) {
            indexName = ElasticConst.ELASTIC_INDEX_PREFIX + subProject;
        }
        return indexName;
    }

    /**
     * 根据index名解析获取subprojectId
     * @param index
     * @return
     */
    @Override
    public Long getSubProjectIdByIndex(String index) {
        if (com.boot.commons.utils.StringUtils.isEmpty(index)){
            String subProjectId = index.substring(index.lastIndexOf("_"));
            return com.boot.commons.utils.StringUtils.isEmpty(subProjectId) ? 0L : Long.parseLong(subProjectId);
        }

        return 0L;
    }

    /**
     * 创建es索引
     * @param index es索引名称
     * @param elasticSetting setting配置
     * @return
     */
    @Override
    public boolean addIndex(String index, ElasticSetting elasticSetting) throws IOException {
        // 1、setting设置
        Map<String, Object> settingMap = new HashMap<>();
        // 设置索引刷新时间间隔
        settingMap.put("refresh_interval", elasticSetting.getRefreshInterval());
        settingMap.put("number_of_shards", elasticSetting.getShardNumber());
        settingMap.put("number_of_replicas", elasticSetting.getReplicasNumber());
        // 设置专病索引mapping最大字段数量限制
        settingMap.put("mapping.total_fields.limit", elasticSetting.getTotalFieldLimit());

        // 2、mapping设置
        Map<String, Object> mappingMap = new HashMap<>();
        // 获取默认mapping字段配置
        XContentBuilder defaultMapping = buildIndexMapping();
        String defaultMappingStr = Strings.toString(defaultMapping);
        JSONObject docObject = JSONObject.parseObject(defaultMappingStr);
        // 设置动态mapping模板
        String templateMapping = Strings.toString(buildDynamicTemplatesMapping());
        JSONObject jsonObject = JSONObject.parseObject(templateMapping);
        docObject.putAll(jsonObject);
        mappingMap.put("doc", docObject);

        return ElasticManager.index().createIndex(index, settingMap, mappingMap);
    }

    /**
     * 添加mapping字段
     * @param index es索引
     * @param fieldName 字段名称
     * @param fieldType 字段类型
     * @return
     */
    @Override
    public boolean addField(String index, String fieldName, String fieldType) {
        boolean ret;
        ElasticFieldType elasticFieldType = new ElasticFieldType(fieldName, fieldType);
        boolean exists = ElasticManager.index().indexExists(index);
        if (exists) {
            ret = ElasticManager.index().addField(index, elasticFieldType);
        } else {
            throw new BusinessException("索引不存在");
        }
        return ret;
    }

    /**
     * 判断索引是否存在
     * @param indices
     * @return
     */
    @Override
    public boolean indexExists(String... indices) {
        return ElasticManager.index().indexExists(indices);
    }

    /**
     * 删除es索引
     * @param indices es索引名称
     * @return
     * @throws IOException
     */
    @Override
    public boolean deleteIndex(String... indices) {
        return ElasticManager.index().deleteIndex(indices);
    }

    /**
     * 生成动态mapping模板
     * 以object_、array_、select_、radio_、checkbox_前缀开头的字段除外，其他动态生成的字段默认为keyword类型
     */
    private XContentBuilder buildDynamicTemplatesMapping() throws IOException {
        XContentBuilder builder = jsonBuilder()
                .startObject()
                .startArray(ElasticConst.ELASTIC_DYNAMICTEMPLATES)
                .startObject()
                .startObject("pattern_as_keywords")
                .field("match", ELASTIC_KEYWORD_FIELD_REGEX)
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
                .startObject()
                .startObject("pattern_as_object")
                .field("match", ELASTIC_OBJECT_FIELD_REGEX)
                .field("match_pattern", "regex")
                .startObject("mapping")
                .field("type", "object")
                .endObject()
                .endObject()
                .endObject()
                .startObject()
                .startObject("date_as_string")
                .field("match_mapping_type", "date")
                .startObject("mapping")
                .field("type", "keyword")
                .endObject()
                .endObject()
                .endObject()
                .endArray()
                .endObject();

        System.out.println(Strings.toString(builder));
        return builder;
    }

    /**
     * 设置index的mapping
     */
    private XContentBuilder buildIndexMapping() throws IOException {
        XContentBuilder builder = jsonBuilder()
                .startObject()
                .startObject(ElasticConst.ELASTIC_FIELD_ROUTING)
                .field("required", true)
                .endObject()
                .startObject("properties");

        // 设置join关系
        builder.startObject(ElasticConst.ELASTIC_FIELD_JION_FIELD);
        builder.field("type", "join");
        builder.field("eager_global_ordinals", true);
        builder.startObject("relations");
        builder.field(ElasticConst.ELASTIC_FIELD_PATIENT, Arrays.asList(ElasticConst.ELASTIC_FIELD_CRF, ElasticConst.ELASTIC_FIELD_ATTACHMENT, ElasticConst.ELASTIC_FIELD_SAMPLE));
        builder.endObject();
        builder.endObject();

        // 登记号
        builder.startObject(ElasticConst.ELASTIC_FIELD_REGNO);
        builder.field("type", "keyword");
        builder.endObject();
        // 病人唯一标识
        builder.startObject(ElasticConst.ELASTIC_FIELD_EMPI);
        builder.field("type", "keyword");
        builder.endObject();
        // docId
        builder.startObject(ElasticConst.ELASTIC_FIELD_DOC_ID);
        builder.field("type", "keyword");
        builder.endObject();
        // sourceId
        builder.startObject(ElasticConst.ELASTIC_SOURCE_ID);
        builder.field("type", "keyword");
        builder.endObject();
        // 表单填写数据
        builder.startObject(ElasticConst.ELASTIC_FIELD_FILL_DATA);
        builder.field("type", "object");
        builder.endObject();
        // 表单form_guid
        builder.startObject(ElasticConst.ELASTIC_FIELD_FORM_GUID);
        builder.field("type", "keyword");
        builder.endObject();
        // 子项目id
        builder.startObject(ElasticConst.ELASTIC_FIELD_SUB_PROJECT_ID);
        builder.field("type", "keyword");
        builder.endObject();
        // 随访id
        builder.startObject(ElasticConst.ELASTIC_FIELD_VISIT_ID);
        builder.field("type", "keyword");
        builder.endObject();
        // 附件信息
        builder.startObject(ElasticConst.ELASTIC_FIELD_ATTACHMENT);
        builder.field("type", "object");
        builder.endObject();
        // 病例信息相关字段
        List<String> patField = getPatField();
        for (String field : patField) {
            if (ElasticConst.ELASTIC_PAT_FIELD_BIRTHDAY.equals(field)){
                DateFieldMetadata dateFieldMetadata = new DateFieldMetadata(field);
                builder.startObject(dateFieldMetadata.getName());
                builder.field("type", dateFieldMetadata.getType());
                builder.field("format", dateFieldMetadata.getFormat());
                // 默认忽略格式错误的文档
                builder.field("ignore_malformed", true);
                builder.endObject();
                continue;
            }

            builder.startObject(field);
            builder.field("type", "keyword");
            builder.startObject("fields");
            builder.startObject("keyword");
            builder.field("type", "keyword");
            builder.endObject();
            builder.endObject();
            builder.endObject();
        }
        // 增量时间戳字段 timestamp
        DateFieldMetadata dateFieldMetadata = new DateFieldMetadata(ElasticConst.ELASTIC_FIELD_TIMESTAMP);
        builder.startObject(dateFieldMetadata.getName());
        builder.field("type", dateFieldMetadata.getType());
        builder.field("format", dateFieldMetadata.getFormat());
        // 默认忽略格式错误的文档
        builder.field("ignore_malformed", true);
        builder.endObject();

        builder.endObject().endObject();

        return builder;
    }

    /**
     * 获取病例基本信息字段
     * @return
     */
    private List<String> getPatField(){
        List<String> patField = new ArrayList<>();
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_GENDER);
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_BIRTHDAY);
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_ID_CARD);
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_RECORD_ID);
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_TEL);
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_NAME);
        patField.add(ElasticConst.ELASTIC_PAT_FIELD_SOURCE);
        return patField;
    }

}
