package com.learning.es.model;

import com.learning.core.utils.StringUtils;
import com.learning.es.enums.ESFieldTypeEnum;
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
     * 查询字段
     */
    private String queryString;
    /**
     * 查询对象列表
     */
    private List<FilterQuery> filtersQueries;
    /**
     * 字段map
     * string为表名
     * PropertyMapper为属性对象
     */
    private Map<String, List<PropertyMapper>> fields;
    private Map<String, PropertyMapper> textMapper;
    /**
     * 基本查询
     */
    private transient QueryBuilder rootTypeQueryBuilder;
    /**
     * 对查询结果进行次级查询
     */
    private transient QueryBuilder secondTypeQueryBuilder;
    private transient QueryBuilder thirdTypeQueryBuilder;
    private transient QueryStringQueryBuilder queryStringQueryBuilder;
    /**
     * 布尔查询构造器
     */
    private transient QueryBuilder filterQueryBuilder;
    /**
     * 高亮查询构造器
     */
    private transient HighlightBuilder highlightBuilder;

    private QuickConditionBuilder(String index, String queryString, List<FilterQuery> filterQuery, Map<String, List<PropertyMapper>> fields, Map<String, PropertyMapper> textMapper) {
        this.index = index;
        this.queryString = queryString;
        this.fields = fields;
        this.textMapper = textMapper;
        this.filtersQueries = filterQuery;
    }

    public void build() {
        String patientTableName = ConfigProperties.getKey("es.query.table.patient");
        String admTableName = ConfigProperties.getKey("es.query.table.adm");
        //高亮字段
        List<String> curFields = new ArrayList<>();
        BoolQueryBuilder secondBool = new BoolQueryBuilder();
        String filterField = null;
        //搜索构造器列表
        List<QueryBuilder> docTypeQuery = new ArrayList<>();

        if (this.filtersQueries != null && this.filtersQueries.size() > 0) {
            BoolQueryBuilder filterBool = new BoolQueryBuilder();
            //第一层查询条件
            List<QueryBuilder> oneLevel = new ArrayList<>();
            //第二层查询条件
            List<QueryBuilder> secondLevel = new ArrayList<>();
            //第三层查询条件
            List<QueryBuilder> thirdLevel = new ArrayList<>();

            Iterator<FilterQuery> it = this.filtersQueries.iterator();

            label142:
            while(true) {
                //查询字段类型
                String curTypeName;
                //查询构造器
                QueryBuilder query;

                //构造查询器
                while(true) {
                    FilterQuery filterQuery;

                    //循环到start有值时，对查询构造器进行构造
                    do {
                        if (! it.hasNext()) {
                            QueryBuilder oBool;
                            if (oneLevel.size() > 0) {
                                oBool = createBoolQuery(oneLevel, "and");
                                filterBool.should(oBool);
                            }

                            if (secondLevel.size() > 0) {
                                oBool = createBoolQuery(secondLevel, "and");
                                filterBool.should(oBool);
                            }

                            if (thirdLevel.size() > 0) {
                                oBool = createBoolQuery(thirdLevel, "and");
                                filterBool.should(oBool);
                            }

                            this.filterQueryBuilder = filterBool;
                            break label142;
                        }

                        filterQuery = it.next();
                        curTypeName = filterQuery.getType();

                    } while(filterQuery.getStart() == null);

                    query = null;
                    //搜索类型
                    String curQueryType = filterQuery.getQueryType();
                    String curV;

                    //搜索字段类型为精确分词查询时
                    if ("term".equals(curQueryType)) {
                        curV = filterQuery.getField();

                        //字段为性别时
                        if (! StringUtils.isEmpty(curV)
                                && curV
                                .equals(
                                        ConfigProperties
                                                .getKey("es.query.field.patient.gender")
                                )
                        ) {
                            BoolQueryBuilder bqb = QueryBuilders.boolQuery();
                            if (curV.contains("男")) {
                                bqb.should(QueryBuilders.termQuery(curV, "男"));
                                bqb.should(QueryBuilders.termQuery(curV, "男性"));
                                query = bqb;
                            } else if (curV.contains("女")) {
                                bqb.should(QueryBuilders.termQuery(curV, "女"));
                                bqb.should(QueryBuilders.termQuery(curV, "女性"));
                                query = bqb;
                            } else {
                                query = QueryBuilders.termQuery(curV, curV);
                            }
                            break;
                        }
                        
                        //不为性别时
                        query = QueryBuilders.termQuery(
                                filterQuery.getField(),
                                filterQuery.getStart()
                        );

                        break;
                    }

                    //范围搜索（大于等于start值）
                    if ("range".equals(curQueryType)) {
                        RangeQueryBuilder cQuery = QueryBuilders
                                .rangeQuery(
                                        filterQuery
                                                .getField()
                                )
                                .from(
                                        filterQuery.getStart(),
                                        true
                                );

                        //设置范围搜索终止值
                        if (filterQuery.getEnd() != null) {
                            cQuery.to(filterQuery.getEnd(), true);
                        }

                        query = cQuery;
                        break;
                    }

                    if (! "type"
                            .equals(curQueryType)
                            && !"docType"
                            .equals(curQueryType)
                    ) {
                        break;
                    }

                    //其余情况全为联合搜索
                    curV = filterQuery.getStart();
                    if (! StringUtils.isEmpty(curV)) {

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
                }

                //设置相应的构造
                if (patientTableName.equals(curTypeName)) {
                    oneLevel
                            .add(query);
                    secondLevel
                            .add(
                                    new HasParentQueryBuilder(
                                            curTypeName,
                                            query,
                                            false
                                    )
                            );
                    thirdLevel
                            .add(
                                    new HasParentQueryBuilder(
                                            admTableName,
                                            new HasParentQueryBuilder(
                                                    curTypeName,
                                                    query,
                                                    false
                                            ),
                                            false
                                    )
                            );
                } else if (admTableName.equals(curTypeName)) {
                    oneLevel.add(new HasChildQueryBuilder(curTypeName, query, ScoreMode.Max));
                    secondLevel.add(query);
                    thirdLevel.add(new HasParentQueryBuilder(curTypeName, query, false));
                } else if (!"all".equals(curTypeName)) {
                    oneLevel.add(new HasChildQueryBuilder(admTableName, new HasChildQueryBuilder(curTypeName, query, ScoreMode.Max), ScoreMode.Max));
                    secondLevel.add(new HasChildQueryBuilder(curTypeName, query, ScoreMode.Max));
                    thirdLevel.add(query);
                }
            }
        } else {
            this.filterQueryBuilder = null;
        }

        Iterator it = this.fields.entrySet().iterator();

        label120:
        while(it.hasNext()) {
            Map.Entry<String, List<PropertyMapper>> entry = (Map.Entry)it.next();
            String typeName = entry.getKey();
            List<PropertyMapper> objs = entry.getValue();
            List<String> cfs = new ArrayList<>();
            Iterator<PropertyMapper> itPropertyMapper = objs.iterator();

            while(true) {
                PropertyMapper mapper;

                do {
                    if (! itPropertyMapper.hasNext()) {
                        curFields.addAll(cfs);
                        QueryStringQueryBuilder curQueryStringBr = buildQueryString(this.queryString, cfs);
                        if (patientTableName.equals(typeName)) {
                            secondBool
                                    .should(
                                            new HasParentQueryBuilder(
                                                    typeName,
                                                    curQueryStringBr,
                                                    false
                                            )
                                    );
                        } else if (admTableName.equals(typeName)) {
                            secondBool.should(curQueryStringBr);
                        } else {
                            secondBool
                                    .should(
                                            new HasChildQueryBuilder(
                                                    typeName,
                                                    curQueryStringBr,
                                                    ScoreMode.Max
                                            )
                                    );
                        }
                        continue label120;
                    }

                    mapper = itPropertyMapper.next();
                } while(StringUtils.isEmpty(filterField)
                        && ! filterField.equals(mapper.getProperty()));

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

        QueryStringQueryBuilder queryBuilder = buildQueryString(this.queryString, curFields);
        HighlightBuilder highlightBuilder = buildHighlightQuery(curFields);
        this.queryStringQueryBuilder = queryBuilder;
        this.highlightBuilder = highlightBuilder;
        BoolQueryBuilder filter = new BoolQueryBuilder();
        filter.filter(secondBool);

        //设置次级查询过滤器
        if (this.filterQueryBuilder != null) {
            filter.filter(this.filterQueryBuilder);
        }

        this.rootTypeQueryBuilder = new HasChildQueryBuilder(admTableName, filter, ScoreMode.Max);
        this.secondTypeQueryBuilder = filter;
        this.thirdTypeQueryBuilder = new HasParentQueryBuilder(admTableName, filter, false);

        if (docTypeQuery.size() > 0) {
            BoolQueryBuilder rootLevelBool = new BoolQueryBuilder();
            rootLevelBool.must(this.rootTypeQueryBuilder).must(docTypeQuery.get(0));
            this.rootTypeQueryBuilder = rootLevelBool;
            BoolQueryBuilder secondLevelBool = new BoolQueryBuilder();
            secondLevelBool.must(this.secondTypeQueryBuilder).must(docTypeQuery.get(0));
            this.secondTypeQueryBuilder = secondLevelBool;
            BoolQueryBuilder thirdLevelBool = new BoolQueryBuilder();
            thirdLevelBool.must(this.thirdTypeQueryBuilder).must(docTypeQuery.get(0));
            this.thirdTypeQueryBuilder = thirdLevelBool;

            if (docTypeQuery.size() > 0) {
                BoolQueryBuilder filterLevelBool = new BoolQueryBuilder();
                filterLevelBool.must(this.filterQueryBuilder).must(docTypeQuery.get(0));
                this.filterQueryBuilder = filterLevelBool;
            }
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

    public QueryBuilder getRootTypeQueryBuilder() {
        return this.rootTypeQueryBuilder;
    }

    public QueryBuilder getSecondTypeQueryBuilder() {
        return this.secondTypeQueryBuilder;
    }

    public QueryBuilder getThirdTypeQueryBuilder() {
        return this.thirdTypeQueryBuilder;
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
