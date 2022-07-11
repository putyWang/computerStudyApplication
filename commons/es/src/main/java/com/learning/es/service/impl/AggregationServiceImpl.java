package com.learning.es.service.impl;

import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.ConfigProperties;
import com.learning.es.model.condition.AdvancedConditionBuilder;
import com.learning.es.service.AggregationService;
import com.learning.es.service.EsServiceImpl;
import com.learning.es.service.QueryService;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.range.RangeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AggregationServiceImpl
        extends EsServiceImpl
        implements AggregationService {

    private QueryService queryService;

    public AggregationServiceImpl(RestClientFactory restClientFactory) {
        super(restClientFactory);
        this.queryService = new QueryServiceImpl(restClientFactory);
    }

    public Map<String, Object> docTypeCountAggr(String index, QueryBuilder secondQueryBuilder)
            throws Exception {

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        QueryBuilder thirdQueryBuilder = AdvancedConditionBuilder
                .resolveThirdConditionGroup(secondQueryBuilder);

        BoolQueryBuilder queryBool = new BoolQueryBuilder();

        queryBool
                .should(secondQueryBuilder)
                .should(thirdQueryBuilder);
        boolQueryBuilder.must(queryBool);
        CardinalityAggregationBuilder valueCountAggregation = AggregationBuilders
                .cardinality("admno_count")
                .field("admno");

        TermsAggregationBuilder aggregation = AggregationBuilders
                .terms("docType")
                .field("join_field")
                .subAggregation(valueCountAggregation)
                .order(BucketOrder.count(false))
                .size(10);

        TermsAggregationBuilder admTypeAggregation = AggregationBuilders.terms("admType").field(ConfigProperties.getKey("es.query.field.adm.type")).order(BucketOrder.count(false)).size(10);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder).aggregation(aggregation).aggregation(admTypeAggregation).size(0);
        Map<String, Object> result = new HashMap<>();
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types("doc");
        searchRequest.source(sourceBuilder);
        SearchResponse response = this.client.search(searchRequest, RequestOptions.DEFAULT);
        long curAdmNoTotal = 0L;
        Terms admTypeAgg = (Terms)response.getAggregations().get("admType");
        Iterator var16 = admTypeAgg.getBuckets().iterator();

        while(var16.hasNext()) {
            Terms.Bucket entry = (Terms.Bucket)var16.next();
            String key = entry.getKey().toString();
            long docCount = entry.getDocCount();
            curAdmNoTotal += docCount;
            String curK = this.getAdmTypeKey(key);
            if (curK != null) {
                result.put(curK, docCount);
            }
        }

        Terms docTypeAgg = response.getAggregations().get("docType");

        String percent;
        String key;
        for(Iterator var30 = docTypeAgg.getBuckets().iterator(); var30.hasNext(); result.put(key, percent + "%")) {
            Terms.Bucket entry = (Terms.Bucket)var30.next();
            key = entry.getKey().toString();
            long docCount = entry.getDocCount();
            Cardinality admCountAgg = entry.getAggregations().get("admno_count");
            long curAdmCount = admCountAgg.getValue();
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            percent = numberFormat.format((float)curAdmCount / (float)curAdmNoTotal * 100.0F);

            try {
                if (percent != null && Float.parseFloat(percent) >= 100.0F) {
                    percent = "100.00";
                }
            } catch (Exception var28) {
                var28.printStackTrace();
            }
        }

        return result;
    }

    public Map<String, Object> aggrByRegNos(List<String> regNos) {
        Map<String, Object> result = new HashMap();
        if (regNos != null && regNos.size() != 0) {
            String defaultIndex = ConfigProperties.getKey("es.query.index.name");
            String medicalRecord = ConfigProperties.getKey("es.query.table.adm");
            String regNo = "regno";
            String joinField = "join_field";
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(regNo, regNos);
            boolQueryBuilder.filter(termsQueryBuilder);
            boolQueryBuilder.filter(QueryBuilders.termsQuery(joinField, new String[]{medicalRecord}));
            TermsAggregationBuilder aggregation = ((TermsAggregationBuilder)AggregationBuilders.terms("regNo").field(regNo)).size(regNos.size());
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(boolQueryBuilder).aggregation(aggregation).size(0);
            SearchRequest searchRequest = new SearchRequest(new String[]{defaultIndex});
            searchRequest.types(new String[]{"doc"}).routing((String[])regNos.toArray(new String[0])).source(sourceBuilder);
            SearchResponse response = null;

            try {
                response = this.client.search(searchRequest, RequestOptions.DEFAULT);
            } catch (IOException var19) {
                var19.printStackTrace();
            }

            if (response != null) {
                Terms agg = (Terms)response.getAggregations().get("regNo");
                Iterator var14 = agg.getBuckets().iterator();

                while(var14.hasNext()) {
                    Terms.Bucket entry = (Terms.Bucket)var14.next();
                    String key = entry.getKey().toString();
                    long docCount = entry.getDocCount();
                    result.put(key, docCount);
                }
            }

            return result;
        } else {
            return result;
        }
    }

    public LinkedHashMap<String, Object> aggrTop(String index, QueryBuilder queryBuilder, String docType, String aggreField, Integer size, Boolean flag) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.filter(QueryBuilders.termQuery("join_field", docType));
        if (queryBuilder != null) {
            boolQueryBuilder.must(queryBuilder);
        }

        TermsAggregationBuilder aggregation = ((TermsAggregationBuilder)AggregationBuilders.terms(docType).collectMode(Aggregator.SubAggCollectionMode.BREADTH_FIRST).field(aggreField)).order(BucketOrder.count(false)).size(20);
        return this.aggrDoc(index, boolQueryBuilder, aggregation, docType, size, flag);
    }

    public Map<String, Object> aggrAdmCount(String index, String docType, String aggreField, String countField, String timeField) {
        String joinField = "join_field";
        String indexType = "doc";
        String patient = ConfigProperties.getKey("es.query.table.patient");
        Map<String, Object> result = new HashMap();
        Map<String, Map<String, Object>> data = new HashMap();
        SearchResponse response = null;
        BoolQueryBuilder patientCountQuery = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery(joinField, patient));
        long allPatient = 0L;
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        QueryBuilder docTypeQuery = QueryBuilders.termQuery(joinField, docType);
        boolQueryBuilder.filter(docTypeQuery);
        CardinalityAggregationBuilder countAggregation = AggregationBuilders.cardinality("count").field(countField);
        MaxAggregationBuilder maxAggregationBuilder = AggregationBuilders.max("max").field(timeField).format("yyyy-MM-dd");
        MinAggregationBuilder minAggregationBuilder = AggregationBuilders.min("min").field(timeField).format("yyyy-MM-dd");
        TermsAggregationBuilder aggregation = AggregationBuilders.terms("aggr").field(aggreField).subAggregation(countAggregation).subAggregation(maxAggregationBuilder).subAggregation(minAggregationBuilder).size(20);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder).aggregation(aggregation).size(0);
        SearchRequest searchRequest = new SearchRequest(new String[]{index});
        searchRequest.types(new String[]{indexType});
        searchRequest.source(sourceBuilder);

        try {
            response = this.client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException var38) {
            var38.printStackTrace();
        }

        if (response == null) {
            return result;
        } else {
            allPatient = this.queryService.count(patientCountQuery, new String[]{index});
            Terms aggr = (Terms)response.getAggregations().get("aggr");
            Iterator var24 = aggr.getBuckets().iterator();

            while(var24.hasNext()) {
                Terms.Bucket entry = (Terms.Bucket)var24.next();
                Map<String, Object> curMap = new HashMap();
                String key = entry.getKey().toString();
                long docCount = entry.getDocCount();
                Cardinality countAgg = (Cardinality)entry.getAggregations().get("count");
                long patientCount = countAgg.getValue();
                Max max = entry.getAggregations().get("max");
                Min min = entry.getAggregations().get("min");
                String maxTime = max.getValueAsString();
                String minTime = min.getValueAsString();
                curMap.put("firstAdmTime", minTime);
                curMap.put("lastAdmTime", maxTime);
                curMap.put("admCount", docCount);
                curMap.put("patientCount", patientCount);
                String admTypeKey = this.getAdmTypeKey(key);
                if (admTypeKey != null) {
                    data.put(admTypeKey, curMap);
                }
            }

            result.put("allPatient", allPatient);
            result.put("data", data);
            return result;
        }
    }

    public Map<String, Object> aggrAdmPatientCount(String index, String docType, String aggreField) {
        String joinField = "join_field";
        String indexType = "doc";
        String regNo = "regno";
        String patient = ConfigProperties.getKey("es.query.table.patient");
        Map<String, Object> result = new HashMap();
        List<Map<String, Object>> data = new ArrayList();
        SearchResponse response = null;
        BoolQueryBuilder patientCountQuery = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery(joinField, patient));
        long allPatient = 0L;
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        QueryBuilder docTypeQuery = QueryBuilders.termQuery(joinField, docType);
        boolQueryBuilder.filter(docTypeQuery);
        CardinalityAggregationBuilder countAggregation = (CardinalityAggregationBuilder)AggregationBuilders.cardinality("count").field(regNo);
        TermsAggregationBuilder aggregation = ((TermsAggregationBuilder)((TermsAggregationBuilder)AggregationBuilders.terms("aggr").field(aggreField)).subAggregation(countAggregation)).size(20);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder).aggregation(aggregation).size(0);
        SearchRequest searchRequest = new SearchRequest(new String[]{index});
        searchRequest.types(new String[]{indexType});
        searchRequest.source(sourceBuilder);

        try {
            response = this.client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException var29) {
            var29.printStackTrace();
        }

        if (response == null) {
            return result;
        } else {
            allPatient = this.queryService.count(patientCountQuery, new String[]{index});
            Terms aggr = (Terms)response.getAggregations().get("aggr");
            Iterator var21 = aggr.getBuckets().iterator();

            while(var21.hasNext()) {
                Terms.Bucket entry = (Terms.Bucket)var21.next();
                Map<String, Object> curMap = new HashMap();
                String key = entry.getKey().toString();
                Cardinality countAgg = (Cardinality)entry.getAggregations().get("count");
                long patientCount = countAgg.getValue();
                String admTypeKey = this.getAdmTypeKey(key);
                if (admTypeKey != null) {
                    curMap.put("admType", admTypeKey);
                    curMap.put("patientCount", patientCount);
                    data.add(curMap);
                }
            }

            result.put("total", allPatient);
            result.put("data", data);
            return result;
        }
    }

    public Map<String, Object> aggrSex(String index, QueryBuilder rootQueryBuilder, String docType, String sexField) {
        Map<String, Object> result = new HashMap();
        TermsAggregationBuilder sexAggregation = ((TermsAggregationBuilder)AggregationBuilders.terms("sex").field(sexField)).order(BucketOrder.count(false)).size(10);
        Map<String, Object> sexMap = this.aggrDoc(index, rootQueryBuilder, sexAggregation, "sex", (Integer)null, false);
        if (sexMap != null && sexMap.size() > 0) {
            Long maleCount = (sexMap.get("男") == null ? 0L : (Long)sexMap.get("男")) + (sexMap.get("男性") == null ? 0L : (Long)sexMap.get("男性"));
            Long femaleCount = (sexMap.get("女") == null ? 0L : (Long)sexMap.get("女")) + (sexMap.get("女性") == null ? 0L : (Long)sexMap.get("女性"));
            sexMap.remove("男");
            sexMap.remove("男性");
            sexMap.remove("女");
            sexMap.remove("女性");
            result.put("男", maleCount);
            result.put("女", femaleCount);
            if (sexMap.size() > 0) {
                Long otherCount = 0L;

                Object curCount;
                for(Iterator var11 = sexMap.values().iterator(); var11.hasNext(); otherCount = otherCount + (Long)curCount) {
                    curCount = var11.next();
                }

                result.put("未知的性别", otherCount);
            }
        }

        return result;
    }

    public LinkedHashMap<String, Object> aggrAge(String index, QueryBuilder rootQueryBuilder, String docType, String field) {
        RangeAggregationBuilder rangeAggregationBuilder = ((RangeAggregationBuilder)AggregationBuilders.range("ageRange").field(field)).addUnboundedTo("0-6", 6.0D).addRange("6-18", 6.0D, 18.0D).addRange("18-40", 18.0D, 40.0D).addRange("40-65", 40.0D, 65.0D).addUnboundedFrom(">65", 65.0D);
        return this.aggrDoc(index, rootQueryBuilder, rangeAggregationBuilder, "ageRange", (Integer)null, false);
    }

    public List<Map<String, Object>> aggrSexAndAdmAge(String index) {
        String genderField = ConfigProperties.getKey("es.query.field.patient.gender");
        String parentTable = ConfigProperties.getKey("es.query.table.patient");
        String medicalRecord = ConfigProperties.getKey("es.query.table.adm");
        String joinField = "join_field";
        String admAge = ConfigProperties.getKey("es.query.field.adm.age");
        String regNoField = "regno";
        List<Map<String, Object>> result = new ArrayList();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        TermQueryBuilder docTypeQuery = QueryBuilders.termQuery(joinField, medicalRecord);
        boolQueryBuilder.filter(docTypeQuery);
        HasParentQueryBuilder maleQuery = new HasParentQueryBuilder(parentTable, new TermQueryBuilder(genderField, "男"), false);
        HasParentQueryBuilder femaleQuery = new HasParentQueryBuilder(parentTable, new TermQueryBuilder(genderField, "女"), false);
        CardinalityAggregationBuilder countAggregation = (CardinalityAggregationBuilder)AggregationBuilders.cardinality("patientCount").field(regNoField);
        RangeAggregationBuilder rangeAggregationBuilder = ((RangeAggregationBuilder)((RangeAggregationBuilder)AggregationBuilders.range("ageRange").field(admAge)).subAggregation(countAggregation)).addUnboundedTo("10岁以下", 10.0D).addRange("10-20", 10.0D, 20.0D).addRange("20-30", 20.0D, 30.0D).addRange("30-40", 30.0D, 40.0D).addRange("40-50", 40.0D, 50.0D).addRange("50-60", 50.0D, 60.0D).addRange("60-70", 60.0D, 70.0D).addRange("70-80", 70.0D, 80.0D).addUnboundedFrom("80岁以上", 80.0D);
        FilterAggregationBuilder maleAggr = (FilterAggregationBuilder)AggregationBuilders.filter("male", maleQuery).subAggregation(rangeAggregationBuilder);
        FilterAggregationBuilder femaleAggr = (FilterAggregationBuilder)AggregationBuilders.filter("female", femaleQuery).subAggregation(rangeAggregationBuilder);
        SearchResponse response = null;
        SearchSourceBuilder sourceBuilder = (new SearchSourceBuilder()).query(boolQueryBuilder).aggregation(maleAggr).aggregation(femaleAggr).size(0);
        SearchRequest searchRequest = (new SearchRequest(new String[]{index})).types(new String[]{"doc"}).source(sourceBuilder);

        try {
            response = this.client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException var37) {
            var37.printStackTrace();
        }

        if (response == null) {
            return result;
        } else {
            Filter maleFilter = (Filter)response.getAggregations().get("male");
            Range maleRange = (Range)maleFilter.getAggregations().get("ageRange");
            Filter femaleFilter = (Filter)response.getAggregations().get("female");
            Range femaleRange = (Range)femaleFilter.getAggregations().get("ageRange");
            Iterator var24 = maleRange.getBuckets().iterator();

            while(var24.hasNext()) {
                org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket male = (org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket)var24.next();
                String maleKey = male.getKey().toString();
                Cardinality maleCard = (Cardinality)male.getAggregations().get("patientCount");
                long malePatientCount = maleCard == null ? 0L : maleCard.getValue();
                Iterator var30 = femaleRange.getBuckets().iterator();

                while(var30.hasNext()) {
                    org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket female = (org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket)var30.next();
                    String femaleKey = female.getKey().toString();
                    Cardinality femaleCard = (Cardinality)female.getAggregations().get("patientCount");
                    long femalePatientCount = femaleCard == null ? 0L : femaleCard.getValue();
                    if (maleKey.equals(femaleKey)) {
                        Map<String, Object> curMap = new HashMap();
                        curMap.put("ageRange", maleKey);
                        curMap.put("maleCount", malePatientCount);
                        curMap.put("femaleCount", femalePatientCount);
                        result.add(curMap);
                    }
                }
            }

            return result;
        }
    }

    public List<Map<String, Object>> aggrPatientAdmCount(String index) {
        String medicalRecord = ConfigProperties.getKey("es.query.table.adm");
        String regNoField = "regno";
        String joinField = "join_field";
        String admType = ConfigProperties.getKey("es.query.field.adm.type");
        List<Map<String, Object>> result = new ArrayList();
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        TermQueryBuilder docTypeQuery = QueryBuilders.termQuery(joinField, medicalRecord);
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(admType, "住院");
        boolQueryBuilder.filter(docTypeQuery).filter(termQueryBuilder);
        TermsAggregationBuilder regNoAggregation = ((TermsAggregationBuilder)AggregationBuilders.terms("regNo").field(regNoField)).order(BucketOrder.count(false)).size(2147483647);
        LinkedHashMap<String, Object> resultMap = this.aggrDoc(index, boolQueryBuilder, regNoAggregation, "regNo", (Integer)null, false);
        Map<Long, Long> groupMap = (Map)resultMap.values().stream().collect(Collectors.groupingBy((value) -> {
            return (Long)value;
        }, Collectors.counting()));
        Map<String, Object> once = new HashMap<String, Object>() {
            {
                this.put("freq", "1次");
                this.put("patientCount", 0L);
            }
        };
        Map<String, Object> twice = new HashMap<String, Object>() {
            {
                this.put("freq", "2次");
                this.put("patientCount", 0L);
            }
        };
        Map<String, Object> threeTimes = new HashMap<String, Object>() {
            {
                this.put("freq", "3次");
                this.put("patientCount", 0L);
            }
        };
        Map<String, Object> fourTimes = new HashMap<String, Object>() {
            {
                this.put("freq", "4次");
                this.put("patientCount", 0L);
            }
        };
        Map<String, Object> fiveTimes = new HashMap<String, Object>() {
            {
                this.put("freq", "5次");
                this.put("patientCount", 0L);
            }
        };
        Map<String, Object> moreThanfiveTimes = new HashMap<String, Object>() {
            {
                this.put("freq", "5次以上");
                this.put("patientCount", 0L);
            }
        };
        groupMap.forEach((key, value) -> {
            switch(key.intValue()) {
                case 1:
                    once.put("patientCount", value);
                    break;
                case 2:
                    twice.put("patientCount", value);
                    break;
                case 3:
                    threeTimes.put("patientCount", value);
                    break;
                case 4:
                    fourTimes.put("patientCount", value);
                    break;
                case 5:
                    fiveTimes.put("patientCount", value);
                    break;
                default:
                    long paitentCount = moreThanfiveTimes.get("patientCount") != null ? Long.parseLong(moreThanfiveTimes.get("patientCount").toString()) : 0L;
                    paitentCount += value;
                    moreThanfiveTimes.put("patientCount", paitentCount);
            }

        });
        result.add(once);
        result.add(twice);
        result.add(threeTimes);
        result.add(fourTimes);
        result.add(fiveTimes);
        result.add(moreThanfiveTimes);
        return result;
    }

    public LinkedHashMap<String, Object> aggrAdmDate(String index, QueryBuilder rootQueryBuilder, String docType, String field) {
        DateHistogramAggregationBuilder dateRangeAggregationBuilder = (DateHistogramAggregationBuilder)((DateHistogramAggregationBuilder)AggregationBuilders.dateHistogram("admDate").field(field)).dateHistogramInterval(DateHistogramInterval.YEAR).format("yyyy");
        return this.aggrDoc(index, rootQueryBuilder, dateRangeAggregationBuilder, "admDate", (Integer)null, false);
    }

    public LinkedHashMap<String, Object> aggrAdmDateHistogram(String index, QueryBuilder queryBuilder, String field, String expression, String format) {
        DateHistogramAggregationBuilder dateHistogram = (DateHistogramAggregationBuilder)((DateHistogramAggregationBuilder)AggregationBuilders.dateHistogram("dateHistogram").field(field)).dateHistogramInterval(new DateHistogramInterval(expression)).format(format);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).aggregation(dateHistogram).size(10);
        LinkedHashMap result = new LinkedHashMap();

        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{index});
            searchRequest.types(new String[]{"doc"});
            searchRequest.source(sourceBuilder);
            SearchResponse response = this.client.search(searchRequest, RequestOptions.DEFAULT);
            Histogram hisAgg = (Histogram)response.getAggregations().get("dateHistogram");
            Iterator var12 = hisAgg.getBuckets().iterator();

            while(var12.hasNext()) {
                org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket entry = (org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket)var12.next();
                new HashMap();
                String key = entry.getKeyAsString();
                long docCount = entry.getDocCount();
                result.put(key, docCount);
            }
        } catch (IOException var18) {
            var18.printStackTrace();
        }

        return result;
    }

    public String aggrTimeMax(String index, QueryBuilder queryBuilder, String field) {
        MaxAggregationBuilder maxAggregationBuilder = (MaxAggregationBuilder)((MaxAggregationBuilder)AggregationBuilders.max("max").field(field)).format("yyyy-MM-dd HH:mm:ss");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).aggregation(maxAggregationBuilder).size(0);
        String maxTime = null;

        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{index});
            searchRequest.types(new String[]{"doc"});
            searchRequest.source(sourceBuilder);
            SearchResponse response = this.client.search(searchRequest, RequestOptions.DEFAULT);
            Max max = (Max)response.getAggregations().get("max");
            Double value = max.getValue();
            boolean flag = value.isInfinite();
            maxTime = flag ? "" : max.getValueAsString();
        } catch (IOException var12) {
            var12.printStackTrace();
        }

        return maxTime;
    }

    public String aggrTimeMin(String index, QueryBuilder queryBuilder, String field) {
        MinAggregationBuilder minAggregationBuilder = (MinAggregationBuilder)((MinAggregationBuilder)AggregationBuilders.min("min").field(field)).format("yyyy-MM-dd HH:mm:ss");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).aggregation(minAggregationBuilder).size(0);
        String minTime = null;

        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{index});
            searchRequest.types(new String[]{"doc"});
            searchRequest.source(sourceBuilder);
            SearchResponse response = this.client.search(searchRequest, RequestOptions.DEFAULT);
            Min min = response.getAggregations().get("min");
            Double value = min.getValue();
            boolean flag = value.isInfinite();
            minTime = flag ? "" : min.getValueAsString();
        } catch (IOException var12) {
            var12.printStackTrace();
        }

        return minTime;
    }

    private LinkedHashMap<String, Long> getBucketsAsMap(Terms aggTerms) {
        if (aggTerms == null) {
            return null;
        } else {
            LinkedHashMap<String, Long> bucketmap = new LinkedHashMap();
            Iterator var3 = aggTerms.getBuckets().iterator();

            while(var3.hasNext()) {
                Terms.Bucket entry = (Terms.Bucket)var3.next();
                String key = entry.getKey().toString();
                long docCount = entry.getDocCount();
                bucketmap.put(key, docCount);
            }

            return bucketmap;
        }
    }

    private LinkedHashMap<String, Object> aggrDoc(String index, QueryBuilder queryBuilder, AggregationBuilder aggregationBuilder, String aggrName, Integer aggrSize, Boolean flag) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(queryBuilder).aggregation(aggregationBuilder).size(0);
        LinkedHashMap<String, Object> result = new LinkedHashMap();
        SearchRequest searchRequest = new SearchRequest(new String[]{index});
        searchRequest.types(new String[]{"doc"});
        searchRequest.source(sourceBuilder);
        SearchResponse response = null;

        try {
            response = this.client.search(searchRequest, RequestOptions.DEFAULT);
            MultiBucketsAggregation diagAgg = (MultiBucketsAggregation)response.getAggregations().get(aggrName);
            long curDocTypeTotal = response.getHits().getTotalHits().value;
            if (curDocTypeTotal > 0L) {
                Iterator var14 = diagAgg.getBuckets().iterator();

                label51:
                do {
                    while(var14.hasNext()) {
                        org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket entry = (org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation.Bucket)var14.next();
                        String key = entry.getKey().toString();
                        long docCount = entry.getDocCount();
                        if (!flag) {
                            result.put(key, docCount);
                            continue label51;
                        }

                        NumberFormat numberFormat = NumberFormat.getInstance();
                        numberFormat.setMaximumFractionDigits(2);
                        String percent = numberFormat.format((double)((float)docCount / (float)curDocTypeTotal * 100.0F));
                        String ignoreName = ConfigProperties.getKey("es.aggregation.ignore.diagnose");
                        if (StringUtils.isNotEmpty(ignoreName)) {
                            String[] ignoreNameArr = ignoreName.split(",");
                            if (ConfigProperties.getKey("es.query.table.medicine").equals(aggrName)) {
                                boolean isFilter = false;
                                String[] var24 = ignoreNameArr;
                                int var25 = ignoreNameArr.length;

                                for(int var26 = 0; var26 < var25; ++var26) {
                                    String n = var24[var26];
                                    if (key.contains(n)) {
                                        isFilter = true;
                                        break;
                                    }
                                }

                                if (isFilter) {
                                    continue;
                                }
                            }
                        }

                        result.put(key, percent);
                        continue label51;
                    }

                    return result;
                } while(aggrSize == null || result.size() != aggrSize);
            }
        } catch (IOException var28) {
            var28.printStackTrace();
        }

        return result;
    }

    private String getAdmTypeKey(String admTypeValue) {
        String admTypeKey = null;
        byte var4 = -1;
        switch(admTypeValue.hashCode()) {
            case 656333:
                if (admTypeValue.equals("体检")) {
                    var4 = 3;
                }
                break;
            case 666656:
                if (admTypeValue.equals("其他")) {
                    var4 = 4;
                }
                break;
            case 667891:
                if (admTypeValue.equals("住院")) {
                    var4 = 1;
                }
                break;
            case 798789:
                if (admTypeValue.equals("急诊")) {
                    var4 = 2;
                }
                break;
            case 1225442:
                if (admTypeValue.equals("门诊")) {
                    var4 = 0;
                }
        }

        switch(var4) {
            case 0:
                admTypeKey = "admO";
                break;
            case 1:
                admTypeKey = "admI";
                break;
            case 2:
                admTypeKey = "admE";
                break;
            case 3:
                admTypeKey = "admH";
                break;
            case 4:
                admTypeKey = "admOther";
        }

        return admTypeKey;
    }
}
