package com.learning.es.model.condition;

import com.learning.es.enums.LogicEnum;
import com.learning.es.enums.RelativeTypeEnum;
import com.learning.es.model.ConfigProperties;
import com.learning.es.model.FieldItemInfo;
import com.learning.es.utils.tree.RegionBeanTree;
import com.learning.es.utils.tree.TreeNode;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class AdvancedConditionBuilder
        extends ConditionBuildBase
        implements Serializable {

    private static final long serialVersionUID = 1L;
    private String index;
    private Map<String, PropertyMapper> propertyMapperMap;
    private transient QueryBuilder rootTypeQueryBuilder;
    private transient QueryBuilder secondTypeQueryBuilder;
    private transient QueryBuilder thirdTypeQueryBuilder;

    private AdvancedConditionBuilder(List<TreeNode> childParent, QueryConditionGroup queryConditionGroups, Map<String, PropertyMapper> propertyMapperMap, String indexName) {
        this.propertyMapperMap = propertyMapperMap;
        this.index = indexName;
    }

    public static AdvancedConditionBuilder builder(QueryConditionGroup queryConditionGroupArr, List<TreeNode> typeTree, Map<String, PropertyMapper> propertyMapperMap, String index) throws Exception {
        AdvancedConditionBuilder builder = new AdvancedConditionBuilder(typeTree, queryConditionGroupArr, propertyMapperMap, index);
        return builder;
    }

    public static AdvancedConditionBuilder builder(QueryConditionGroup queryConditionGroup, List<RegionBeanTree> typeTree, Map<String, PropertyMapper> propertyMapperMap) {
        AdvancedConditionBuilder builder = null;
        return builder;
    }

    public static AdvancedConditionBuilder builder(QueryConditionGroup bringintoCond, QueryConditionGroup rulingoutCond, List<TreeNode> typeTree, Map<String, PropertyMapper> propertyMapperMap, String index) throws Exception {
        AdvancedConditionBuilder builder = new AdvancedConditionBuilder(typeTree, bringintoCond, propertyMapperMap, index);
        return builder;
    }

    public static QueryBuilder resolveThirdConditionGroup(QueryBuilder secondQueryBuilder) throws Exception {
        if (secondQueryBuilder == null) {
            return null;
        } else {
            List<String> secondTypes = new ArrayList<>();
            secondTypes.add(ConfigProperties.getKey("es.query.table.adm"));
            new BoolQueryBuilder();
            List<QueryBuilder> builders = new ArrayList<>();

            for (String t : secondTypes) {
                QueryBuilder queryBuilder = new HasParentQueryBuilder(t, secondQueryBuilder, false);
                builders.add(queryBuilder);
            }

            return createBoolQuery(builders, LogicEnum.OR);
        }
    }

    private static QueryBuilder getQueryByCondition(QueryCondition queryCondition) {
        FieldItemInfo fieldItemInfo = queryCondition.getFieldItemInfo();
        String relativeCode = queryCondition.getRelative();
        Object value = queryCondition.getValue();
        Object end = queryCondition.getEndValue();
        QueryBuilder queryBuilder = createItemQuery(fieldItemInfo, relativeCode, value, end);
        FieldItemInfo otherFieldItemInfo = queryCondition.getOtherFieldItemInfo();
        String curOtherFieldCode = otherFieldItemInfo != null ? otherFieldItemInfo.getFieldCode() : null;
        if (StringUtils.isNotEmpty(curOtherFieldCode)) {
            QueryBuilder otherQueryBuilder = createItemQuery(otherFieldItemInfo, RelativeTypeEnum.EQUAL.getCode(), queryCondition.getOtherValue(), (Object)null);
            if (queryBuilder != null && otherQueryBuilder != null) {
                BoolQueryBuilder itemBool = new BoolQueryBuilder();
                itemBool.must(queryBuilder).must(otherQueryBuilder);
                return itemBool;
            } else {
                return null;
            }
        } else {
            return queryBuilder;
        }
    }

    public String getIndex() {
        return this.index;
    }

    public Map<String, PropertyMapper> getPropertyMapperMap() {
        return this.propertyMapperMap;
    }

    public QueryBuilder getRootTypeQueryBuilder() {
        return this.rootTypeQueryBuilder;
    }

    public QueryBuilder getSecondTypeQueryBuilder() {
        return this.secondTypeQueryBuilder;
    }

    public QueryBuilder getThirdTypeQueryBuilder() {
        return this.thirdTypeQueryBuilder;
    }
}
