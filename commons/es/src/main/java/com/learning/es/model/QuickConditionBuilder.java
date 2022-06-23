package com.learning.es.model;

import com.learning.core.utils.StringUtils;
import com.learning.es.enums.ESFieldTypeEnum;
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

public final class QuickConditionBuilder extends ConditionBuildBase implements Serializable {
    private static final long serialVersionUID = 1L;
    private String index;
    private String queryString;
    private List<FilterQuery> filtersQueries;
    private Map<String, List<PropertyMapper>> fields;
    private Map<String, PropertyMapper> textMapper;
    private transient QueryBuilder rootTypeQueryBuilder;
    private transient QueryBuilder secondTypeQueryBuilder;
    private transient QueryBuilder thirdTypeQueryBuilder;
    private transient QueryStringQueryBuilder queryStringQueryBuilder;
    private transient QueryBuilder filterQueryBuilder;
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
        List<String> curFields = new ArrayList();
        BoolQueryBuilder secondBool = new BoolQueryBuilder();
        String filterField = null;
        List<QueryBuilder> docTypeQuery = new ArrayList();
        if (this.filtersQueries != null && this.filtersQueries.size() > 0) {
            BoolQueryBuilder filterBool = new BoolQueryBuilder();
            List<QueryBuilder> oneLevel = new ArrayList();
            List<QueryBuilder> secoundLevel = new ArrayList();
            List<QueryBuilder> thirdLevel = new ArrayList();
            Iterator var11 = this.filtersQueries.iterator();

            label142:
            while(true) {
                String curTypeName;
                Object query;
                while(true) {
                    FilterQuery filterQuery;
                    do {
                        if (!var11.hasNext()) {
                            QueryBuilder oBool;
                            if (oneLevel.size() > 0) {
                                oBool = createBoolQuery(oneLevel, "and");
                                filterBool.should(oBool);
                            }

                            if (secoundLevel.size() > 0) {
                                oBool = createBoolQuery(secoundLevel, "and");
                                filterBool.should(oBool);
                            }

                            if (thirdLevel.size() > 0) {
                                oBool = createBoolQuery(thirdLevel, "and");
                                filterBool.should(oBool);
                            }

                            this.filterQueryBuilder = filterBool;
                            break label142;
                        }

                        filterQuery = (FilterQuery)var11.next();
                        curTypeName = filterQuery.getType();
                    } while(filterQuery.getStart() == null);

                    query = null;
                    String curQueryType = filterQuery.getQueryType();
                    String curV;
                    if ("term".equals(curQueryType)) {
                        curV = filterQuery.getField();
                        curV = filterQuery.getStart();
                        if (! StringUtils.isEmpty(curV) && curV.equals(ConfigProperties.getKey("es.query.field.patient.gender"))) {
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

                        query = QueryBuilders.termQuery(filterQuery.getField(), filterQuery.getStart());
                        break;
                    }

                    if ("range".equals(curQueryType)) {
                        RangeQueryBuilder cquery = QueryBuilders.rangeQuery(filterQuery.getField()).from(filterQuery.getStart(), true);
                        if (filterQuery.getEnd() != null) {
                            cquery.to(filterQuery.getEnd(), true);
                        }

                        query = cquery;
                        break;
                    }

                    if (!"type".equals(curQueryType) && !"docType".equals(curQueryType)) {
                        break;
                    }

                    curV = filterQuery.getStart();
                    if (curV != null && !"".equals(curV)) {
                        if (curV.contains(".")) {
                            String[] arr = curV.split("\\.");
                            if (arr.length >= 2) {
                                filterField = arr[1];
                                query = QueryBuilders.termQuery("join_field", arr[0]);
                            }
                        } else {
                            query = QueryBuilders.termQuery("join_field", curV);
                        }

                        docTypeQuery.add((QueryBuilder)query);
                    }
                }

                if (patientTableName.equals(curTypeName)) {
                    oneLevel.add((QueryBuilder)query);
                    secoundLevel.add(new HasParentQueryBuilder(curTypeName, (QueryBuilder)query, false));
                    thirdLevel.add(new HasParentQueryBuilder(admTableName, new HasParentQueryBuilder(curTypeName, (QueryBuilder)query, false), false));
                } else if (admTableName.equals(curTypeName)) {
                    oneLevel.add(new HasChildQueryBuilder(curTypeName, (QueryBuilder)query, ScoreMode.Max));
                    secoundLevel.add((QueryBuilder)query);
                    thirdLevel.add(new HasParentQueryBuilder(curTypeName, (QueryBuilder)query, false));
                } else if (!"all".equals(curTypeName)) {
                    oneLevel.add(new HasChildQueryBuilder(admTableName, new HasChildQueryBuilder(curTypeName, (QueryBuilder)query, ScoreMode.Max), ScoreMode.Max));
                    secoundLevel.add(new HasChildQueryBuilder(curTypeName, (QueryBuilder)query, ScoreMode.Max));
                    thirdLevel.add((QueryBuilder)query);
                }
            }
        } else {
            this.filterQueryBuilder = null;
        }

        Iterator var19 = this.fields.entrySet().iterator();

        label120:
        while(var19.hasNext()) {
            Map.Entry<String, List<PropertyMapper>> entry = (Map.Entry)var19.next();
            String typeName = entry.getKey();
            List<PropertyMapper> objs = entry.getValue();
            List<String> cfs = new ArrayList<>();
            Iterator var29 = objs.iterator();

            while(true) {
                PropertyMapper mapper;
                do {
                    if (!var29.hasNext()) {
                        curFields.addAll(cfs);
                        QueryStringQueryBuilder curQueryStringBr = buildQueryString(this.queryString, cfs);
                        if (patientTableName.equals(typeName)) {
                            secondBool.should(new HasParentQueryBuilder(typeName, curQueryStringBr, false));
                        } else if (admTableName.equals(typeName)) {
                            secondBool.should(curQueryStringBr);
                        } else {
                            secondBool.should(new HasChildQueryBuilder(typeName, curQueryStringBr, ScoreMode.Max));
                        }
                        continue label120;
                    }

                    mapper = (PropertyMapper)var29.next();
                } while(filterField != null && !"".equals(filterField) && !filterField.equals(mapper.getProperty()));

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
        if (this.filterQueryBuilder != null) {
            filter.filter(this.filterQueryBuilder);
        }

        this.rootTypeQueryBuilder = new HasChildQueryBuilder(admTableName, filter, ScoreMode.Max);
        this.secondTypeQueryBuilder = filter;
        this.thirdTypeQueryBuilder = new HasParentQueryBuilder(admTableName, filter, false);
        if (docTypeQuery.size() > 0) {
            BoolQueryBuilder rootLevelBool = new BoolQueryBuilder();
            rootLevelBool.must(this.rootTypeQueryBuilder).must((QueryBuilder)docTypeQuery.get(0));
            this.rootTypeQueryBuilder = rootLevelBool;
            BoolQueryBuilder secondLevelBool = new BoolQueryBuilder();
            secondLevelBool.must(this.secondTypeQueryBuilder).must((QueryBuilder)docTypeQuery.get(0));
            this.secondTypeQueryBuilder = secondLevelBool;
            BoolQueryBuilder thirdLevelBool = new BoolQueryBuilder();
            thirdLevelBool.must(this.thirdTypeQueryBuilder).must((QueryBuilder)docTypeQuery.get(0));
            this.thirdTypeQueryBuilder = thirdLevelBool;
            if (docTypeQuery.size() > 0) {
                BoolQueryBuilder filterLevelBool = new BoolQueryBuilder();
                filterLevelBool.must(this.filterQueryBuilder).must((QueryBuilder)docTypeQuery.get(0));
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
