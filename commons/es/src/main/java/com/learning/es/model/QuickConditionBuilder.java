package com.learning.es.model;

import com.learning.core.utils.StringUtils;
import com.learning.es.enums.ESFieldTypeEnum;
import com.learning.es.enums.LogicEnum;
import com.learning.es.enums.QueryTypeEnum;
import com.learning.es.model.condition.ConditionBuildBase;
import com.learning.es.model.condition.FilterQuery;
import com.learning.es.model.condition.PropertyMapper;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 查询条件生成器
 */
public final class QuickConditionBuilder
        extends ConditionBuildBase
        implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 索引
     */
    private String index;
    /**
     * 查询字符串
     */
    private String queryString;
    /**
     * 查询对象列表
     */
    private List<FilterQuery> filtersQueries;


    private BoolQueryBuilder boolQueryBuilder;
    /**
     * 字段map
     * string为表名
     * PropertyMapper为属性对象
     */
    private Map<String, List<PropertyMapper>> fields;
    private Map<String, PropertyMapper> textMapper;
    private transient QueryStringQueryBuilder queryStringQueryBuilder;
    /**
     * 布尔查询构造器
     */
    private transient QueryBuilder filterQueryBuilder;
    /**
     * 高亮查询构造器
     */
    private transient HighlightBuilder highlightBuilder;

    private QuickConditionBuilder(
            String index,
            String queryString,
            List<FilterQuery> filterQuery,
            Map<String, List<PropertyMapper>> fields,
            Map<String, PropertyMapper> textMapper
    ) {
        this.index = index;
        this.queryString = queryString;
        this.fields = fields;
        this.textMapper = textMapper;
        this.filtersQueries = filterQuery;
    }

    public void build() {

        //高亮字段
        List<String> curFields = new ArrayList<>();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        String filterField = null;
        //查询构造器列表
        List<QueryBuilder> docTypeQuery = new ArrayList<>();

        if (this.filtersQueries != null && this.filtersQueries.size() > 0) {
            BoolQueryBuilder filterBool = new BoolQueryBuilder();
            //查询条件列表
            List<QueryBuilder> queryList = new ArrayList<>();

            Iterator<FilterQuery> it = this
                    .filtersQueries
                    .iterator();

            label142:
            while(true) {
                //查询字段类型
                String curTypeName;
                //查询构造器
                QueryBuilder query;

                //构造查询器
                label143:
                while (true) {
                    FilterQuery filterQuery;

                    //循环到start有值时，对查询构造器进行构造
                    do {
                        if (!it.hasNext()) {
                            this.filterQueryBuilder = filterBool
                                    .should(createBoolQuery(queryList, LogicEnum.AND));

                            break label142;
                        }

                        filterQuery = it.next();
                        //获取查询字段名称
                        curTypeName = filterQuery.getType();

                    } while (filterQuery.getStart() == null);

                    query = null;
                    //搜索类型
                    QueryTypeEnum curQueryType = filterQuery.getQueryType();
                    String curV;

                    //构造非模糊搜索
                    switch (curQueryType) {
                        //搜索字段类型为精确分词查询时
                        case TERM:
                            query = QueryBuilders
                                    .termQuery(filterQuery.getField(), filterQuery.getStart());

                            break label143;

                        //范围搜索（大于等于start值）
                        case RANGE:
                            RangeQueryBuilder cQuery = QueryBuilders
                                    .rangeQuery(filterQuery.getField())
                                    .from(filterQuery.getStart(), true);

                            //设置范围搜索终止值
                            if (filterQuery.getEnd() != null) {
                                cQuery.to(filterQuery.getEnd(), true);
                            }

                            query = cQuery;

                            break label143;
                        case TYPE:
                        case DOCTYPE:
                            //其余情况全为联合搜索
                            curV = filterQuery.getStart();
                            if (!StringUtils.isEmpty(curV)) {

                                if (curV.contains(".")) {
                                    String[] arr = curV.split("\\.");
                                    if (arr.length >= 2) {
                                        filterField = arr[1];
                                        query = QueryBuilders.termQuery("join_field", arr[0]);
                                    }
                                } else {
                                    query = QueryBuilders.termQuery("join_field", curV);
                                }

                                docTypeQuery.add(query);
                            }

                            break;
                        default:
                            //剩下为模糊搜索
                            break;
                    }
                }

                //添加对应的查询构造器
                queryList.add(query);

            }
        } else {
            this.filterQueryBuilder = null;
        }

        Iterator<Map.Entry<String, List<PropertyMapper>>> it = this.fields.entrySet().iterator();

        //构造模糊搜索
        label120:
        while(it.hasNext()) {
            Map.Entry<String, List<PropertyMapper>> entry = it.next();
            String typeName = entry.getKey();
            List<PropertyMapper> objs = entry.getValue();
            //字段列表
            List<String> cfs = new ArrayList<>();
            Iterator<PropertyMapper> itPropertyMapper = objs.iterator();

            while(true) {
                PropertyMapper mapper;

                do {
                    //字段获取完成后设置bool查询
                    if (! itPropertyMapper.hasNext()) {
                        curFields.addAll(cfs);

                        boolQueryBuilder.should(buildQueryString(this.queryString, cfs));

                        continue label120;
                    }

                    mapper = itPropertyMapper.next();
                } while(StringUtils.isEmpty(filterField)
                        && ! filterField.equals(mapper.getProperty()));

                //字段类型为TEXT和KEYWORD时设置模糊查询
                ESFieldTypeEnum fieldTypeEnum = mapper.getPropertyType();
                if (fieldTypeEnum != null) {
                    switch(fieldTypeEnum) {
                        case TEXT:
                        case KEYWORD:
                            cfs.add(mapper.getProperty());
                    }
                }
            }
        }
        this.queryStringQueryBuilder = buildQueryString(this.queryString, curFields);
        this.highlightBuilder = buildHighlightQuery(curFields);
        //设置布尔查询构造器
        this.boolQueryBuilder
                .filter(boolQueryBuilder)
                .filter(this.filterQueryBuilder);

        if (docTypeQuery.size() > 0) {
            BoolQueryBuilder filterLevelBool = new BoolQueryBuilder();
            filterLevelBool
                    .must(this.filterQueryBuilder)
                    .must(docTypeQuery.get(0));
            this.filterQueryBuilder = filterLevelBool;
        }

    }

    public static QuickConditionBuilder builder(String index, String queryString, List<FilterQuery> filterQuery, Map<String, List<PropertyMapper>> fields, Map<String, PropertyMapper> textMapper) {
        QuickConditionBuilder builder = new QuickConditionBuilder(index, queryString, filterQuery, fields, textMapper);
        builder.build();
        return builder;
    }

    public String getIndex() {
        return this.index;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public List<FilterQuery> getFiltersQueries() {
        return this.filtersQueries;
    }

    public void setFiltersQueries(List<FilterQuery> filtersQuerys) {
        this.filtersQueries = filtersQuerys;
    }

    public Map<String, PropertyMapper> getTextMapper() {
        return this.textMapper;
    }

    public Map<String, List<PropertyMapper>> getFields() {
        return this.fields;
    }

    public BoolQueryBuilder getBoolQueryBuilder() {
        return boolQueryBuilder;
    }

    public QueryStringQueryBuilder getQueryStringQueryBuilder() {
        return this.queryStringQueryBuilder;
    }

    public QueryBuilder getFilterQueryBuilder() {
        return this.filterQueryBuilder;
    }

    public HighlightBuilder getHighlightBuilder() {
        return this.highlightBuilder;
    }
}
