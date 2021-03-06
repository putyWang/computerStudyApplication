package com.learning.es.model.condition;

import com.learning.core.exception.SpringBootException;
import com.learning.es.constants.ElasticConst;
import com.learning.es.enums.AnalyzerEnum;
import com.learning.es.enums.LogicEnum;
import com.learning.es.enums.RelativeTypeEnum;
import com.learning.es.model.FieldItemInfo;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 基础条件构造类
 *
 * @author wangpenghui
 */
public class ConditionBuildBase {
    /**
     * 构建高亮查询
     *
     * @param fields
     * @return
     */
    public static HighlightBuilder buildHighlightQuery(List<String> fields) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style=\"color:red\">").postTags("</span>");
        highlightBuilder.fragmentSize(30); //设置要显示出来的文本片段的长度，默认是100
        highlightBuilder.numOfFragments(3); //可能高亮的文本片段有多个片段，指定显示几个片段
        highlightBuilder.noMatchSize(5);
        highlightBuilder.highlighterType("plain");   //plain,posting,fvh.
        for (String f : fields) {
            highlightBuilder.field(f);
        }

        return highlightBuilder;
    }

    /**
     * 构建父级条件
     * @param type
     * @param queryBuilder
     * @return
     */
    public static QueryBuilder createParentQuery(String type, QueryBuilder queryBuilder){
        if (queryBuilder == null){
            return null;
        }else {
            return new HasChildQueryBuilder(type, queryBuilder, ScoreMode.Max);
        }

    }

    /**
     * 构建子级条件
     * @param type
     * @param queryBuilder
     * @return
     */
    public static QueryBuilder createChildQuery(String type, QueryBuilder queryBuilder){
        if (queryBuilder == null){
            return null;
        }else {
            return new HasChildQueryBuilder(type, queryBuilder, ScoreMode.Max);
        }

    }

    /**
     * 创建布尔查询条件
     * @param queryBuilders 查询构造器列表
     * @param logic 逻辑名称
     * @return
     */
    public static QueryBuilder createBoolQuery(List<QueryBuilder> queryBuilders, LogicEnum logic) {
        if (queryBuilders == null || queryBuilders.size() == 0) {
            return null;
        } else if (queryBuilders.size() == 1) {
            return queryBuilders.get(0);
        } else {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

            for (QueryBuilder queryBuilder : queryBuilders) {
                if (queryBuilder == null) {
                    continue;
                }
                switch (logic) {
                    case AND:
                        boolQueryBuilder.must(queryBuilder);
                        break;
                    case OR:
                        boolQueryBuilder.should(queryBuilder);
                        break;
                    // not 需要注意的是必须加上docType类型过滤，否则会返回
                    case NOT:
                        boolQueryBuilder.mustNot(queryBuilder);
                        break;
                    // 默认按or
                    default:
                        boolQueryBuilder.should(queryBuilder);
                        break;
                }
            }

            return boolQueryBuilder;
        }
    }

    /**
     * 根据上下限创建区间查询
     * @param itemInfo 查询字段信息
     * @param relativeCode 关联词编码
     * @param start 下限值
     * @param end 上限值
     * @return
     */
    public static QueryBuilder createItemBetweenQuery(FieldItemInfo itemInfo, String relativeCode, Object start, Object end) {
        String fieldName = itemInfo.getFieldName();

        relativeCode = relativeCode.toLowerCase();
        RelativeTypeEnum relativeTypeEnum = RelativeTypeEnum.getByCode(relativeCode);
        QueryBuilder queryBuilder = null;

        if (relativeTypeEnum == null) {
            return null;
        }

        switch (relativeTypeEnum) {
            // 开区间
            case BTEWEENT_OPEN:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).from(start, false).to(end, false);
                break;
            // 闭区间
            case BTEWEENT_OFF:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).from(start, true).to(end, true);
                break;
            // 左开右闭区间
            case BTEWEENT_LEFT:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).from(start, false).to(end, true);
                break;
            // 左闭右开区间
            case BTEWEENT_RIGHT:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).from(start, true).to(end, false);
                break;
            default:
                break;
        }

        return queryBuilder;
    }

    /**
     * 创建查询构造对象
     * @param itemInfo 字段信息
     * @param relativeCode 关联词编码
     * @param start 下限值
     * @param end 上限值
     * @return
     */
    public static QueryBuilder createItemQuery(
            FieldItemInfo itemInfo,
            @NotNull String relativeCode,
            Object start,
            Object end
    ) {

        if (relativeCode == null) {
            throw new SpringBootException("关系词不能为空，请检查");
        }

        if (start == null) {
            start = "";
        }

        String typeName = itemInfo.getTypeName();

        String fieldName = itemInfo.getFieldCode();
        relativeCode = relativeCode.toLowerCase();

        RelativeTypeEnum relativeTypeEnum = RelativeTypeEnum.getByCode(relativeCode);
        QueryBuilder queryBuilder = null;

        if (relativeTypeEnum == null) {
            return null;
        }

        //设置关联词
        switch (relativeTypeEnum) {
            // 等于
            case EQUAL:
                queryBuilder = QueryBuilders.termQuery(fieldName, start);
                break;
            // 不等于
            case NOT_EQUAL:
                queryBuilder = QueryBuilders
                        .boolQuery()
                        .filter(QueryBuilders.termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, typeName))
                        .mustNot(QueryBuilders.termQuery(fieldName, start));
                break;
            //小于
            case LESS_THAN:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).lt(start);
                break;
            // 大于
            case GEATER_THAN:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).gt(start);
                break;
            // 小于等于
            case LESS_THAN_EQUAL:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).lte(start);
                break;
            // 大于等于
            case GEATER_THAN_EQUAL:
                queryBuilder = QueryBuilders.rangeQuery(fieldName).gte(start);
                break;
            // 包含
            case INCLUDE:
                queryBuilder = buildFuzzyQueryString(start.toString(), fieldName);
                break;
            // 不包含
            case NOT_INCLUDE:
                queryBuilder = new BoolQueryBuilder().mustNot(buildFuzzyQueryString(start.toString(), fieldName));
                break;
            // 闭区间
            case BTEWEENT_OFF:
                RangeQueryBuilder rangeOpenQueryBuilder = QueryBuilders.rangeQuery(fieldName).gte(start);
                if (end != null) {
                    rangeOpenQueryBuilder.lte(end);
                }
                queryBuilder = rangeOpenQueryBuilder;

                break;
            // 开区间
            case BTEWEENT_OPEN:
                RangeQueryBuilder rangeEndQueryBuilder = QueryBuilders
                        .rangeQuery(fieldName)
                        .gt(start);
                if (end != null) {
                    rangeEndQueryBuilder.lt(end);
                }
                queryBuilder = rangeEndQueryBuilder;
                break;
            // 左开右闭
            case BTEWEENT_LEFT:
                RangeQueryBuilder rangeLeftQueryBuilder = QueryBuilders
                        .rangeQuery(fieldName)
                        .gt(start);
                if (end != null) {
                    rangeLeftQueryBuilder.lte(end);
                }
                queryBuilder = rangeLeftQueryBuilder;
                break;
            // 左闭右开
            case BTEWEENT_RIGHT:
                RangeQueryBuilder rangeRightQueryBuilder = QueryBuilders
                        .rangeQuery(fieldName)
                        .gte(start);
                if (end != null) {
                    rangeRightQueryBuilder.lt(end);
                }
                queryBuilder = rangeRightQueryBuilder;
                break;
            // 字段值为空查询
            case IS_NULL:
                queryBuilder = QueryBuilders
                        .boolQuery()
                        .mustNot(QueryBuilders.existsQuery(fieldName));
                break;
            // 字段值不为空查询
            case NOT_NULL:
                queryBuilder = QueryBuilders
                        .boolQuery()
                        .filter(
                                QueryBuilders
                                        .termQuery(ElasticConst.ELASTIC_FIELD_JION_FIELD, typeName)
                        )
                        .must(QueryBuilders.existsQuery(fieldName));

                break;
            default:
                break;
        }
        if (queryBuilder == null) {
            throw new SpringBootException("未找到关系词类型");
        }
        return queryBuilder;
    }

    /**
     * 多个字段模糊查询
     * @param queryString 查询字符串
     * @param fields 查询字段列表
     * @return
     */
    public static QueryStringQueryBuilder buildQueryString(String queryString, List<String> fields) {
        QueryStringQueryBuilder queryBuilder = QueryBuilders
                .queryStringQuery(queryString)
                .analyzer(AnalyzerEnum.IK_MAX_WORD.getCode())
                //允许*或?作为第一个字符
                .allowLeadingWildcard(true)
                .defaultOperator(Operator.AND)
                .minimumShouldMatch("90%");

        //添加查询字段
        for (String f : fields) {
            queryBuilder.field(f);
        }

        return queryBuilder;
    }

    /**
     * 单个字段模糊查询
     * @param queryString 查询字段
     * @param field 字段名
     * @return
     */
    public static QueryStringQueryBuilder buildFuzzyQueryString(String queryString, String field) {

        return QueryBuilders
                //允许*或?作为第一个字符
                .queryStringQuery("*" + queryString + "*")
                .allowLeadingWildcard(true)
                .defaultField(field);
    }

}
