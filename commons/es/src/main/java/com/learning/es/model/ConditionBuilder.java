package com.learning.es.model;

import com.learning.es.constants.ElasticConst;
import lombok.Data;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 查询条件构造类
 *
 * @author wangpenghui
 */
@Data
public class ConditionBuilder {
    /**
     * 布尔逻辑
     */
    private String logical;
    /**
     * 当前条件组条件
     */
    private List<QueryFieldInfo> queryFieldInfos = new ArrayList<>();
    /**
     * 子条件列表
     */
    private List<ConditionBuilder> child;
    /**
     * 一级表查询条件，即病人表查询条件
     */
    private transient QueryBuilder rootTypeQueryBuider;
    /**
     * 二级表查询条件，crf表单、附件相关表查询条件
     */
    private transient QueryBuilder secondTypeQueryBuider;

    public void addChild(ConditionBuilder cb) {
        if (this.child == null) {
            this.child = new ArrayList<>();
        }

        this.child.add(cb);
    }

    public void addQueryBuilder(QueryFieldInfo builder) {
        if (builder == null) {
            return;
        }
        if (queryFieldInfos == null) {
            queryFieldInfos = new ArrayList<>();
        }
        this.queryFieldInfos.add(builder);
    }

    public void addQueryBuilders(List<QueryFieldInfo> builders) {
        if (builders == null || builders.size() == 0) {

            return;
        }
        if (queryFieldInfos == null) {
            queryFieldInfos = new ArrayList<>();
        }
        this.queryFieldInfos.addAll(builders);
    }

    /**
     * 生成es 各级表查询条件
     */
    public ConditionBuilder build(){
        this.rootTypeQueryBuider = buildQueryByLevel(1);
        this.secondTypeQueryBuider = buildQueryByLevel(2);

        return this;
    }


    /**
     * 生成es 指定级表查询条件
     * 递归处理
     *
     * @return
     */
    public QueryBuilder buildQueryByLevel(Integer level) {
        QueryBuilder qb = null;
        List<QueryBuilder> qbList = new ArrayList<>();
        List<QueryBuilder> curQbList = resolvConditionGroup(this.queryFieldInfos, level);
        if (!CollectionUtils.isEmpty(curQbList)){
            qbList.addAll(curQbList);
        }

        // 获取子条件列表查询条件
        if (!CollectionUtils.isEmpty(this.child)) {
            for (ConditionBuilder conditionBuilder : this.child) {
                // 递归处理子集查询条件
                QueryBuilder childQueryBuilder = conditionBuilder.buildQueryByLevel(level);
                if (childQueryBuilder != null) {
                    qbList.add(childQueryBuilder);
                }
            }
        }

        if (qbList.size() > 0) {
            // 按条件组逻辑拼接
            qb = ConditionBuildBase.createBoolQuery(qbList, this.logical);
        }

        return qb;
    }

    /**
     * 解析条件组中所有查询条件
     * @param queryFieldInfos
     * @param level
     * @return
     */
    private List<QueryBuilder> resolvConditionGroup(List<QueryFieldInfo> queryFieldInfos, Integer level) {
        // 获取当前条件组所有字段条件
        if (!CollectionUtils.isEmpty(queryFieldInfos)) {
            QueryBuilder qb = null;
            // 同一个表单的查询条件组装到一起
            Map<String, List<QueryFieldInfo>> queryGroup = new HashMap<>();

            // 遍历查询组中的所有查询条件
            for (QueryFieldInfo queryFieldInfo : queryFieldInfos) {
                String formGuid = queryFieldInfo.getFieldItemInfo().getCrf().getFormGuid();
                if (!StringUtils.isEmpty(formGuid)) {
                    if (queryGroup.containsKey(formGuid)){
                        queryGroup.get(formGuid).add(queryFieldInfo);
                    }else {
                        List<QueryFieldInfo> curQueryFieldInfos = new ArrayList<>();
                        curQueryFieldInfos.add(queryFieldInfo);
                        queryGroup.put(formGuid, curQueryFieldInfos);
                    }
                }
            }

            List<QueryBuilder> curQbList = new ArrayList<>();
            Iterator<Map.Entry<String, List<QueryFieldInfo>>> iterator = queryGroup.entrySet().iterator();
            // 遍历不同组之间查询条件
            while (iterator.hasNext()){
                Map.Entry<String, List<QueryFieldInfo>> entry = iterator.next();
                String key = entry.getKey();
                List<QueryFieldInfo> curList = entry.getValue();
                List<QueryBuilder> queryBuilders = getQueryBuilders(curList);
                if (!CollectionUtils.isEmpty(queryBuilders)) {
                    // 按条件组逻辑拼接
                    qb = ConditionBuildBase.createBoolQuery(queryBuilders, this.logical);
                    // 每个表的多个查询条件一起生成对应级别表查询条件
                    String typeName = curList.get(0).getFieldItemInfo().getTypeName();
                    qb = getQueryBuilderByLevel(typeName, qb, level);
                    if (qb != null){
                        curQbList.add(qb);
                    }
                }
            }

            return curQbList;
        }else {
            return null;
        }

    }

    /**
     * 构建指定级别查询条件
     * @param type
     * @param qb
     * @param level
     * @return
     */
    private QueryBuilder getQueryBuilderByLevel(String type, QueryBuilder qb, Integer level) {
        if (qb == null){
            return null;
        }else {
            QueryBuilder queryBuilder = null;
            String patient = ElasticConst.ELASTIC_FIELD_PATIENT;
            if (1 == level){

                if (patient.equals(type)){
                    queryBuilder = qb;
                }else {
                    queryBuilder = ConditionBuildBase.createParentQuery(type, qb);
                }

            }else if (2 == level){

                if (patient.equals(type)){
                    queryBuilder = ConditionBuildBase.createChildQuery(type, qb);
                }else {
                    queryBuilder = qb;
                }
            }

            return queryBuilder;
        }

    }

    /**
     * 批量生成es字段查询条件
     *
     * @param queryFieldInfos es字段查询信息
     * @return
     */
    public List<QueryBuilder> getQueryBuilders(List<QueryFieldInfo> queryFieldInfos) {
        List<QueryBuilder> queryBuilders = new ArrayList<>();

        if (!CollectionUtils.isEmpty(queryFieldInfos)) {
            for (QueryFieldInfo queryFieldInfo : queryFieldInfos) {
                FieldItemInfo fieldItemInfo = queryFieldInfo.getFieldItemInfo();
                String typeName = fieldItemInfo.getTypeName();
                // 设置字段查询条件
                QueryBuilder qb = ConditionBuildBase.createItemQuery(fieldItemInfo,
                        queryFieldInfo.getRelative(), queryFieldInfo.getValue(), queryFieldInfo.getEndValue());
                if (qb == null) {
                    continue;
                }
                if (ElasticConst.ELASTIC_FIELD_CRF.equals(typeName)) {
                    CRFFieldInfo crf = fieldItemInfo.getCrf();
                    // 设置crf表单查询条件,因为不同crf表单可能有相同字段，查询时需要加以区分
                    QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                            .must(qb)
                            .must(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_FORM_GUID, crf.getFormGuid()));

                    queryBuilders.add(queryBuilder);
                } else {
                    queryBuilders.add(qb);
                }
            }
        }

        return queryBuilders;
    }

    public QueryBuilder getRootTypeQueryBuider() {
        return rootTypeQueryBuider;
    }

    public QueryBuilder getSecondTypeQueryBuider() {
        return secondTypeQueryBuider;
    }

}
