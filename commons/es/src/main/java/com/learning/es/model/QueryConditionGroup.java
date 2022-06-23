package com.learning.es.model;

import com.alibaba.fastjson.JSONObject;
import com.learning.es.constants.ElasticConst;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 总查询条件
 * @author wangpenghui
 */
@Data
public class QueryConditionGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    private String logical = "AND";

    private List<QueryCondition> children = new ArrayList<>();

    /**
     * 获取es查询条件
     * @param queryJson 高级检索查询条件
     * @return
     */
    public static ConditionBuilder getConditionBuilder(String queryJson) {
        if (StringUtils.isEmpty(queryJson)){
            return null;
        }else {
            QueryConditionGroup queryConditionGroup = JSONObject.parseObject(queryJson, QueryConditionGroup.class);
            return queryConditionGroup.recombination().build();
        }

    }

    /**
     * 获取es查询条件
     * @param bringIntoCondJson 纳入条件
     * @param rulingOutCondJson 排除条件
     * @return
     */
    public static Map<String, ConditionBuilder> getConditionBuilder(String bringIntoCondJson, String rulingOutCondJson){
        Map<String, ConditionBuilder> conditionBuilderMap = new HashMap<>();
        QueryConditionGroup bringInto = JSONObject.parseObject(bringIntoCondJson, QueryConditionGroup.class);
        QueryConditionGroup rulingOut = JSONObject.parseObject(rulingOutCondJson, QueryConditionGroup.class);

        if (bringInto != null){
            conditionBuilderMap.put("bringInto", bringInto.recombination().build());
        }
        if (rulingOut != null){
            conditionBuilderMap.put("rulingOut", rulingOut.recombination().build());
        }

        return conditionBuilderMap;
    }

    /**
     * 重新组装查询条件
     * @return
     */
    private ConditionBuilder recombination() {
        ConditionBuilder conditionBuilder = new ConditionBuilder();

        String logical = this.logical;
        List<QueryCondition> children = this.children;
        conditionBuilder.setLogical(logical);
        // 遍历子条件组
        for (QueryCondition cond : children) {
            String curT = cond.getType();
            JSONObject query = cond.getQuery();

            if (curT.contains("group")) {
                QueryConditionGroup childG = JSONObject.toJavaObject(query, QueryConditionGroup.class);
                // 递归调用
                ConditionBuilder ccb = childG.recombination();
                conditionBuilder.addChild(ccb);
            } else if (curT.contains("rule")) {
                QueryFieldInfo queryFieldInfo = JSONObject.toJavaObject(query, QueryFieldInfo.class);
                // 转换关联字段
                FieldItemInfo fieldItemInfo = queryFieldInfo.getFieldItemInfo();
                CRFFieldInfo crfFieldInfo = fieldItemInfo.getCrf();
                if (crfFieldInfo == null){
                    continue;
                }
                String formGuid = crfFieldInfo.getFormGuid();
                String path = crfFieldInfo.getPath();
                String fieldCode = "";
                // typename用来标记join_field表信息，现在分别有patient、crf、attachment三个表，其中patient表为一级表，其他为二级表
                String typeName = "";
                if (ElasticConst.ELASTIC_FIELD_PATIENT.equals(formGuid)){
                    fieldCode = path;
                    typeName = formGuid;
                }else if (ElasticConst.ELASTIC_FIELD_ATTACHMENT.equals(formGuid)){
                    fieldCode = StringUtils.isNotEmpty(path) ? (ElasticConst.ELASTIC_FIELD_ATTACHMENT +  "." + path) : path;
                    typeName = formGuid;
                }else {
                    fieldCode = StringUtils.isNotEmpty(path) ? (ElasticConst.ELASTIC_FIELD_FILL_DATA +  "." + path) : path;
                    typeName = ElasticConst.ELASTIC_FIELD_CRF;
                }
                fieldItemInfo.setFieldCode(fieldCode);
                fieldItemInfo.setTypeName(typeName);
                conditionBuilder.addQueryBuilder(queryFieldInfo);
            }
        }

        return conditionBuilder;
    }

}