package com.learning.es.service.impl;

import com.learning.es.bean.AdvancedSearchResult;
import com.learning.es.bean.FulltextSearchResult;
import com.learning.es.bean.HighWeight;
import com.learning.es.bean.SearchResult;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.condition.ConditionBuilder;
import com.learning.es.model.ConfigProperties;
import com.learning.es.model.QuickConditionBuilder;
import com.learning.es.model.condition.PropertyMapper;
import com.learning.es.service.EsServiceImpl;
import com.learning.es.service.QueryService;
import com.learning.es.service.SearchService;
import com.learning.es.utils.DesensitiseUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasChildQueryBuilder;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.util.*;

@Log4j2
public class SearchServiceImpl
        extends EsServiceImpl
        implements SearchService {

    private final QueryService queryService;

    /**
     * 注入对应的es链接
     * @param restClientFactory es链接工厂类
     */
    public SearchServiceImpl(RestClientFactory restClientFactory) {
        super(restClientFactory);
        queryService = new QueryServiceImpl(restClientFactory);
    }


    public FulltextSearchResult quickSearch(QuickConditionBuilder conditionBuilder, int page, int size, String... indices) {
        QueryStringQueryBuilder queryBuilder = conditionBuilder.getQueryStringQueryBuilder();
        HighlightBuilder highlightBuilder = conditionBuilder.getHighlightBuilder();
        QueryBuilder filterQueryBuilder = conditionBuilder.getFilterQueryBuilder();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        int form = (page - 1) * size;
        if (filterQueryBuilder != null) {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.must(queryBuilder).filter(filterQueryBuilder);
            searchSourceBuilder.query(boolQueryBuilder);
        } else {
            searchSourceBuilder.query(queryBuilder);
        }

        searchSourceBuilder.from(form).size(size).highlighter(highlightBuilder);
        this.log.info("全文检索查询条件 {}", searchSourceBuilder.toString());
        SearchHits searchHits = this.queryService.searchForHits(searchSourceBuilder, indices);
        long total = searchHits.getTotalHits().value;
        List<Map<String, Object>> searchHitMaps = new ArrayList();
        List<String> admNos = new ArrayList();
        List<String> regNos = new ArrayList();
        Map<String, PropertyMapper> textMapper = conditionBuilder.getTextMapper();
        String queryKeys = conditionBuilder.getQueryString();
        String[] queryKeyList = queryKeys.split(" ");
        int queryKeyCount = queryKeyList.length;
        Iterator var20 = searchHits.iterator();

        while(var20.hasNext()) {
            SearchHit hit = (SearchHit)var20.next();
            Map<String, Object> curMap = hit.getSourceAsMap();
            String curAdmNo = curMap.getOrDefault("admno", "").toString();
            String curRegNo = curMap.getOrDefault("regno", "").toString();
            if (!StringUtils.isEmpty(curAdmNo)) {
                admNos.add(curAdmNo);
            }

            if (!StringUtils.isEmpty(curRegNo)) {
                regNos.add(curRegNo);
            }

            curMap.put("highlight", this.getHighlightStr(hit, textMapper, queryKeyList));
            searchHitMaps.add(curMap);
        }

        QueryBuilder termsAdmNos = QueryBuilders.termsQuery("admno", admNos);
        QueryBuilder queryPatient = new HasChildQueryBuilder(ConfigProperties.getKey("es.query.table.adm"), termsAdmNos, ScoreMode.Max);
        SearchResult patientResult = this.queryService.search(queryPatient, 100, 0, indices);
        List<Map<String, Object>> patientHits = patientResult.getResultData();
        Map<String, Map<String, Object>> patientMaps = new HashMap<>();
        Iterator var25 = patientHits.iterator();

        while(var25.hasNext()) {
            Map<String, Object> patientMap = (Map)var25.next();
            String curRegNo = patientMap.getOrDefault("regno", "").toString();
            if (!StringUtils.isEmpty(curRegNo)) {
                patientMaps.put(curRegNo, patientMap);
            }
        }

        BoolQueryBuilder queryAdm = new BoolQueryBuilder();
        queryAdm.filter(termsAdmNos);
        queryAdm.must(QueryBuilders.termQuery("join_field", ConfigProperties.getKey("es.query.table.adm")));
        SearchResult admResult = this.queryService.search(queryAdm, 100, 0, indices);
        List<Map<String, Object>> admHits = admResult.getResultData();
        Map<String, Map<String, Object>> admMaps = new HashMap();
        Iterator var29 = admHits.iterator();

        while(var29.hasNext()) {
            Map<String, Object> admMap = (Map)var29.next();
            String curAdmNo = admMap.getOrDefault("admno", "").toString();
            if (!StringUtils.isEmpty(curAdmNo)) {
                admMaps.put(curAdmNo, admMap);
            }
        }

        BoolQueryBuilder queryDiagnose = new BoolQueryBuilder();
        queryDiagnose.filter(termsAdmNos);
        queryDiagnose.must(QueryBuilders.termQuery("join_field", ConfigProperties.getKey("es.query.table.diagnose")));
        SearchResult diagnoseResult = this.queryService.search(queryDiagnose, 100, 0, indices);
        List<Map<String, Object>> diagnoseHits = diagnoseResult.getResultData();
        Map<String, Map<String, String>> diagnoseMaps = new HashMap();
        Iterator var33 = diagnoseHits.iterator();

        while(var33.hasNext()) {
            Map<String, Object> diagMap = (Map)var33.next();
            String curAdmNo = diagMap.getOrDefault("admno", "").toString();
            String curDiagName = diagMap.getOrDefault(ConfigProperties.getKey("es.query.field.diagnose.name"), "").toString();
            String curDiagType = diagMap.getOrDefault(ConfigProperties.getKey("es.query.field.diagnose.type"), "").toString();
            curDiagType = this.getDiagnoseType(curDiagType);
            if (!StringUtils.isEmpty(curAdmNo)) {
                Map<String, String> curMap = (Map)diagnoseMaps.get(curAdmNo);
                if (curMap == null) {
                    curMap = new HashMap();
                }

                if (!StringUtils.isEmpty(curDiagName)) {
                    ((Map)curMap).put(curDiagType, curDiagName);
                }

                diagnoseMaps.put(curAdmNo, curMap);
            }
        }

        FulltextSearchResult result = new FulltextSearchResult();
        result.setTotal(total);
        List<FulltextSearchResult.Result> resultData = new ArrayList<>();

        FulltextSearchResult.Result curR;
        for(Iterator var64 = searchHitMaps.iterator(); var64.hasNext(); resultData.add(curR)) {
            Map<String, Object> map = (Map)var64.next();
            curR = new FulltextSearchResult.Result();
            String curRegNo = map.getOrDefault("regno", "").toString();
            String curAdmNo = map.getOrDefault("admno", "").toString();
            String highLight = map.getOrDefault("highlight", "").toString();
            curR.setRegNo(curRegNo);
            curR.setAdmNo(curAdmNo);
            curR.setHighLight(highLight);
            Map<String, Object> curPatient = patientMaps.get(curRegNo);
            Object curAdmDept;
            if (curPatient != null) {
                Iterator var42 = curPatient.entrySet().iterator();

                while(var42.hasNext()) {
                    Map.Entry<String, Object> entry = (Map.Entry)var42.next();
                    String key = (String)entry.getKey();
                    curAdmDept = entry.getValue();
                    if (curAdmDept != null && curAdmDept instanceof String) {
                        String cValue = curAdmDept.toString();
                        int tuoMinType = textMapper.get(key) == null ? 0 : (textMapper.get(key)).getTuoMinType();
                        String newValue = DesensitiseUtil.tuoMin(tuoMinType, cValue);
                        curPatient.put(key, newValue);
                    }
                }

                String curPatientName = curPatient.getOrDefault(ConfigProperties.getKey("es.query.field.patient.name"), "").toString();
                String curPatientGender = curPatient.getOrDefault(ConfigProperties.getKey("es.query.field.patient.gender"), "").toString();
                curR.setPatientName(curPatientName);
                curR.setPatientGender(curPatientGender);
            }

            Map<String, Object> curAdm = admMaps.get(curAdmNo);
            if (curAdm != null) {
                Iterator var71 = curAdm.entrySet().iterator();

                Object value;
                while(var71.hasNext()) {
                    Map.Entry<String, Object> entry = (Map.Entry)var71.next();
                    String key = (String)entry.getKey();
                    value = entry.getValue();
                    if (value != null && value instanceof String) {
                        String cValue = value.toString();
                        int tuoMinType = textMapper.get(key) == null ? 0 : ((PropertyMapper)textMapper.get(key)).getTuoMinType();
                        String newValue = DesensitiseUtil.tuoMin(tuoMinType, cValue);
                        if (curPatient != null) {
                            curPatient.put(key, newValue);
                        }
                    }
                }

                Object curAdmType = curAdm.getOrDefault(ConfigProperties.getKey("es.query.field.adm.type"), "");
                Object curAdmDate = curAdm.getOrDefault(ConfigProperties.getKey("es.query.field.adm.admdate"), "");
                curAdmDept = curAdm.getOrDefault(ConfigProperties.getKey("es.query.field.adm.admdept"), "");
                value = curAdm.getOrDefault(ConfigProperties.getKey("es.query.field.adm.dishdept"), "");
                Object curDishDate = curAdm.getOrDefault(ConfigProperties.getKey("es.query.field.adm.dishdate"), "");
                curR.setAdmType(curAdmType == null ? "" : curAdmType.toString());
                curR.setAdmDate(curAdmDate == null ? "" : curAdmDate.toString());
                curR.setAdmDept(curAdmDept == null ? "" : curAdmDept.toString());
                curR.setDishDept(value == null ? "" : value.toString());
                curR.setDishDate(curDishDate == null ? "" : curDishDate.toString());
            }

            Map<String, String> curDiags = (Map)diagnoseMaps.get(curAdmNo);
            if (curDiags != null) {
                curR.setDiagName(this.getDiagnoseStr(curDiags));
            }
        }

        result.setData(resultData);
        return result;
    }

    private String getHighlightStr(SearchHit hit, Map<String, PropertyMapper> textMapper, String[] queryKeyList) {
        Collection<HighlightField> highlightFields = hit.getHighlightFields().values();
        StringBuilder sb = new StringBuilder();
        List<HighWeight> highWeights = new ArrayList<>();
        Iterator var7 = highlightFields.iterator();

        while(true) {
            String curProperty;
            StringBuilder curTextStr;
            do {
                if (!var7.hasNext()) {
                    highWeights.sort((o1, o2) -> {
                        return o2.getWeight() - o1.getWeight();
                    });
                    var7 = highWeights.iterator();

                    while(var7.hasNext()) {
                        HighWeight highWeight = (HighWeight)var7.next();
                        sb.append(highWeight.getHighString()).append("&nbsp;");
                    }

                    return sb.toString();
                }

                HighlightField highlightField = (HighlightField)var7.next();
                curProperty = highlightField.getName();
                Text[] text = highlightField.getFragments();
                curTextStr = new StringBuilder();
                Text[] var12 = text;
                int var13 = text.length;

                for(int var14 = 0; var14 < var13; ++var14) {
                    Text str = var12[var14];
                    curTextStr.append(str.toString());
                }
            } while(!curTextStr.toString().contains("</span>"));

            String title = textMapper.get(curProperty) == null ? "" : ((PropertyMapper)textMapper.get(curProperty)).getChineseDesc();
            String curHigh;
            if (title != null && !"".equals(title)) {
                curHigh = title + " - " + curTextStr.toString();
            } else {
                curHigh = curTextStr.toString();
            }

            HighWeight highWeight = new HighWeight();
            highWeight.setHighString(curHigh);
            int w = 0;
            String[] var16 = queryKeyList;
            int var17 = queryKeyList.length;

            for(int var18 = 0; var18 < var17; ++var18) {
                String s = var16[var18];
                if (curHigh.contains(s)) {
                    ++w;
                }
            }

            highWeight.setWeight(w);
            highWeights.add(highWeight);
        }
    }

    public AdvancedSearchResult advancedSearch(ConditionBuilder conditionBuilder, int page, int size, String... indices) throws Exception {
        QueryBuilder queryBuilder = conditionBuilder.toQueryBuilderForPatient(1);
        QueryBuilder querySecondBuilder = conditionBuilder.toQueryBuilderForPatient(2);
        int form = (page - 1) * size;
        SearchResult searchResult = this.queryService.search(queryBuilder, size, form, indices);
        long total = searchResult.getTotal();
        List<Map<String, Object>> searchHitMaps = searchResult.getResultData();
        Map<String, LinkedHashMap<String, Object>> curChildTypeHit = null;
        if (searchHitMaps != null) {
            List<String> regNos = new ArrayList();
            Iterator var14 = ((List)searchHitMaps).iterator();

            while(var14.hasNext()) {
                Map<String, Object> hitMap = (Map)var14.next();
                String curRegNo = hitMap.getOrDefault("regno", "").toString();
                regNos.add(curRegNo);
            }

            BoolQueryBuilder filterBuilder = new BoolQueryBuilder();
            filterBuilder.filter(new TermsQueryBuilder("regno", regNos));
            curChildTypeHit = this.getChildTypeHit(filterBuilder, querySecondBuilder, indices);
        }

        Map<String, PropertyMapper> mapperMap = conditionBuilder.getMapperMap();
        AdvancedSearchResult result = new AdvancedSearchResult();
        result.setTotal(total);
        List<com.dhcc.mrp.elastic.bean.AdvancedSearchResult.Result> results = new ArrayList();
        if (searchHitMaps == null) {
            searchHitMaps = new ArrayList();
        }

        Iterator var36 = ((List)searchHitMaps).iterator();

        while(var36.hasNext()) {
            Map<String, Object> hitMap = (Map)var36.next();
            com.dhcc.mrp.elastic.bean.AdvancedSearchResult.Result curR = new com.dhcc.mrp.elastic.bean.AdvancedSearchResult.Result();
            String curRegNo = hitMap.getOrDefault("regno", "").toString();
            String curPatientName = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.patient.name"), "").toString();
            String curPatientGender = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.patient.gender"), "").toString();
            String curBirthday = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.patient.birthday"), "").toString();
            curR.setRegNo(curRegNo);
            curR.setPatientName(curPatientName);
            curR.setPatientGender(curPatientGender);
            curR.setPatientBirthday(curBirthday);
            LinkedHashMap<String, Object> curHash = curChildTypeHit != null ? (LinkedHashMap)curChildTypeHit.get(curRegNo) : null;
            String dishDateField;
            if (curHash == null) {
                curR.setChildren((List)null);
                curR.setChildTotal(0);
            } else {
                List<Child> childList = new ArrayList();
                int count = 0;

                Child child;
                for(Iterator var26 = curHash.entrySet().iterator(); var26.hasNext(); childList.add(child)) {
                    Map.Entry<String, Object> entry = (Map.Entry)var26.next();
                    ++count;
                    if (count > 5) {
                        break;
                    }

                    HashMap<String, Object> admMap = (HashMap)entry.getValue();
                    child = new Child();
                    if (admMap != null && admMap.size() > 0) {
                        child.setAdmNo(admMap.getOrDefault("admno", "").toString());
                        child.setAdmType(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.type"), "").toString());
                        child.setAdmDept(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.admdept"), "").toString());
                        child.setAdmDate(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.admdate"), "").toString());
                        dishDateField = ConfigProperties.getKey("es.query.field.adm.dishdate");
                        String dishDeptField = ConfigProperties.getKey("es.query.field.adm.dishdept");
                        if (admMap.get(dishDateField) != null) {
                            child.setDishDate(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.dishdate"), "").toString());
                        }

                        if (admMap.get(dishDeptField) != null) {
                            child.setDishDept(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.dishdept"), "").toString());
                        }
                    }
                }

                curR.setChildren(childList);
                curR.setChildTotal(curHash.size());
            }

            Iterator var37 = hitMap.entrySet().iterator();

            while(var37.hasNext()) {
                Map.Entry<String, Object> entry = (Map.Entry)var37.next();
                String key = (String)entry.getKey();
                Object value = entry.getValue();
                if (value instanceof String) {
                    String valueStr = value.toString();
                    int tuoMinType = mapperMap.get(key) == null ? 0 : ((PropertyMapper)mapperMap.get(key)).getTuoMinType();
                    dishDateField = DesensitiseUtil.tuoMin(tuoMinType, valueStr);
                    hitMap.put(key, dishDateField);
                }
            }

            results.add(curR);
        }

        result.setData(results);
        return result;
    }

    public long getPatientCount(ConditionBuilder bringInto, ConditionBuilder rulingOut, QueryBuilder queryBuilder, String... indices) {
        QueryBuilder bringIntoQB = null;
        QueryBuilder rulingOutQB = null;
        if (bringInto != null) {
            bringIntoQB = bringInto.toQueryBuilderForPatient(1);
        }

        if (rulingOut != null) {
            rulingOutQB = rulingOut.toQueryBuilderForPatient(1);
        }

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if (bringIntoQB != null) {
            boolQueryBuilder.must(bringIntoQB);
        }

        if (rulingOutQB != null) {
            boolQueryBuilder.mustNot(rulingOutQB);
        }

        if (queryBuilder != null) {
            boolQueryBuilder.filter(queryBuilder);
        }

        long count = this.queryService.count(boolQueryBuilder, indices);
        return count;
    }

    public List<String> getPatientRegNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, String... indices) {
        return this.getPatientRegNos(bringInto, rulingOut, (QueryBuilder)null, indices);
    }

    public List<String> getPatientRegNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, QueryBuilder queryBuilder, String... indices) {
        BoolQueryBuilder patientQueryBuilder = new BoolQueryBuilder();
        QueryBuilder patientQB;
        if (bringInto != null) {
            patientQB = bringInto.toQueryBuilderForPatient(1);
            if (patientQB != null) {
                patientQueryBuilder.must(patientQB);
            }
        }

        if (rulingOut != null) {
            patientQB = rulingOut.toQueryBuilderForPatient(1);
            if (patientQB != null) {
                patientQueryBuilder.mustNot(patientQB);
            }
        }

        if (queryBuilder != null) {
            patientQueryBuilder.must(queryBuilder);
        }

        List<String> fields = new ArrayList();
        fields.add("regno");
        List<Map<String, Object>> hitsMap = this.queryService.scrollSearch(patientQueryBuilder, (String[])fields.toArray(new String[0]), indices);
        List<String> regNos = new ArrayList();
        Iterator var9 = hitsMap.iterator();

        while(var9.hasNext()) {
            Map<String, Object> map = (Map)var9.next();
            String regNo = map.getOrDefault("regno", "").toString();
            if (StringUtils.isNotEmpty(regNo)) {
                regNos.add(regNo);
            }
        }

        return regNos;
    }

    public List<String> getMedicalRegNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, QueryBuilder queryBuilder, String... indices) {
        BoolQueryBuilder medicalRecordQuery = new BoolQueryBuilder();
        QueryBuilder rulingOutQuery;
        if (bringInto != null) {
            rulingOutQuery = bringInto.toQueryBuilderForPatient(2);
            if (rulingOutQuery != null) {
                medicalRecordQuery.must(rulingOutQuery);
            }
        }

        if (rulingOut != null) {
            rulingOutQuery = rulingOut.toQueryBuilderForPatient(2);
            if (rulingOutQuery != null) {
                medicalRecordQuery.mustNot(rulingOutQuery);
            }
        }

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("join_field", ConfigProperties.getKey("es.query.table.adm"));
        medicalRecordQuery.must(termQueryBuilder);
        if (queryBuilder != null) {
            medicalRecordQuery.must(queryBuilder);
        }

        List<String> fields = new ArrayList();
        fields.add("regno");
        List<Map<String, Object>> hitsMap = this.queryService.scrollSearch(medicalRecordQuery, (String[])fields.toArray(new String[0]), indices);
        Set<String> regNos = new HashSet();
        Iterator var10 = hitsMap.iterator();

        while(var10.hasNext()) {
            Map<String, Object> map = (Map)var10.next();
            String regNo = map.getOrDefault("regno", "").toString();
            if (StringUtils.isNotEmpty(regNo)) {
                regNos.add(regNo);
            }
        }

        return new ArrayList(regNos);
    }

    public SearchResult getAdmNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, String... indices) {
        return this.getAdmNos(bringInto, rulingOut, (String)null, (Integer)null, (Integer)null, (Integer)null, indices);
    }

    public SearchResult getAdmNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, Integer page, Integer size, String... indices) {
        return this.getAdmNos(bringInto, rulingOut, (String)null, (Integer)null, page, size, indices);
    }

    public SearchResult getAdmNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, String tagName, Integer tagValue, String... indices) {
        return this.getAdmNos(bringInto, rulingOut, tagName, tagValue, (Integer)null, (Integer)null, indices);
    }

    public SearchResult getAdmNos(ConditionBuilder bringInto, ConditionBuilder rulingOut, String tagName, Integer tagValue, Integer page, Integer size, String... indices) {
        BoolQueryBuilder admNoQueryBuilder = new BoolQueryBuilder();
        String patient = ConfigProperties.getKey("es.query.table.patient");
        SearchResult searchResult = null;
        QueryBuilder admQB;
        if (bringInto != null) {
            admQB = bringInto.toQueryBuilderForPatient(2);
            if (admQB != null) {
                admNoQueryBuilder.must(admQB);
            }
        }

        if (StringUtil.isNotEmpty(tagName) && tagValue != null && tagValue != 0) {
            HasParentQueryBuilder parentQueryBuilder = new HasParentQueryBuilder(patient, new TermQueryBuilder(tagName, tagValue), false);
            admNoQueryBuilder.filter(parentQueryBuilder);
        }

        if (rulingOut != null) {
            admQB = rulingOut.toQueryBuilderForPatient(2);
            if (admQB != null) {
                admNoQueryBuilder.mustNot(admQB);
            }
        }

        List<String> fields = new ArrayList();
        fields.add("regno");
        fields.add("admno");
        if (page != null && size != null) {
            Integer from = page == 0 ? page : (page - 1) * size;
            searchResult = this.queryService.search(admNoQueryBuilder, (String[])fields.toArray(new String[0]), size, from, indices);
        } else {
            List<Map<String, Object>> result = this.queryService.scrollSearch(admNoQueryBuilder, (String[])fields.toArray(new String[0]), indices);
            searchResult = new SearchResult();
            searchResult.setResultData(result);
        }

        return searchResult;
    }

    public SearchResult getAdmNos(QuickConditionBuilder quickConditionBuilder, String... indices) {
        return this.getAdmNos(quickConditionBuilder, (Integer)null, (Integer)null, indices);
    }

    public SearchResult getAdmNos(QuickConditionBuilder quickConditionBuilder, Integer page, Integer size, String... indices) {
        SearchResult searchResult = null;
        if (quickConditionBuilder != null) {
            QueryBuilder admNoQueryBuilder = quickConditionBuilder.getSecondTypeQueryBuilder();
            if (admNoQueryBuilder != null) {
                List<String> fields = new ArrayList();
                fields.add("regno");
                fields.add("admno");
                if (page != null && size != null) {
                    Integer from = page == 0 ? page : (page - 1) * size;
                    searchResult = this.queryService.search(admNoQueryBuilder, (String[])fields.toArray(new String[0]), size, from, indices);
                } else {
                    List<Map<String, Object>> result = this.queryService.scrollSearch(admNoQueryBuilder, (String[])fields.toArray(new String[0]), indices);
                    searchResult = new SearchResult();
                    searchResult.setResultData(result);
                }
            }
        }

        return searchResult;
    }

    public List<Map<String, Object>> getAdmByRegNos(List<String> regNos, String... indices) {
        List<Map<String, Object>> result = null;
        if (regNos != null && regNos.size() != 0) {
            QueryBuilder termQuery = new TermsQueryBuilder("regno", regNos);
            QueryBuilder typeQuery = new TermsQueryBuilder("join_field", new String[]{ConfigProperties.getKey("es.query.table.adm")});
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder.filter(termQuery).filter(typeQuery);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.constantScoreQuery(boolQueryBuilder));
            sourceBuilder.size(5000);
            SearchRequest searchRequest = new SearchRequest(indices);
            List<String> fields = new ArrayList();
            fields.add("regno");
            fields.add("admno");
            sourceBuilder.fetchSource((String[])fields.toArray(new String[0]), (String[])null);
            searchRequest.source(sourceBuilder);
            result = ElasticManager.query().scrollSearch(searchRequest);
            return result;
        } else {
            return null;
        }
    }

    public void scrollSearchPatient(ElasticMethodInterface methodInterface, ConditionBuilder bringInto, ConditionBuilder rulingOut, QueryBuilder queryBuilder, String... indices) {
        BoolQueryBuilder patientQueryBuilder = new BoolQueryBuilder();
        QueryBuilder patientQB;
        if (bringInto != null) {
            patientQB = bringInto.toQueryBuilderForPatient(1);
            if (patientQB != null) {
                patientQueryBuilder.must(patientQB);
            }
        }

        if (rulingOut != null) {
            patientQB = rulingOut.toQueryBuilderForPatient(1);
            if (patientQB != null) {
                patientQueryBuilder.mustNot(patientQB);
            }
        }

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("join_field", ConfigProperties.getKey("es.query.table.patient"));
        patientQueryBuilder.must(termQueryBuilder);
        if (queryBuilder != null) {
            patientQueryBuilder.must(queryBuilder);
        }

        this.queryService.scrollSearch(methodInterface, patientQueryBuilder, (String[])null, indices);
    }

    public void scrollSearchAdm(ElasticMethodInterface methodInterface, ConditionBuilder bringInto, ConditionBuilder rulingOut, QueryBuilder queryBuilder, String... indices) {
        BoolQueryBuilder admNoQueryBuilder = new BoolQueryBuilder();
        QueryBuilder admQB;
        if (bringInto != null) {
            admQB = bringInto.toQueryBuilderForPatient(2);
            if (admQB != null) {
                admNoQueryBuilder.must(admQB);
            }
        }

        if (rulingOut != null) {
            admQB = rulingOut.toQueryBuilderForPatient(2);
            if (admQB != null) {
                admNoQueryBuilder.mustNot(admQB);
            }
        }

        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("join_field", ConfigProperties.getKey("es.query.table.adm"));
        admNoQueryBuilder.must(termQueryBuilder);
        if (queryBuilder != null) {
            admNoQueryBuilder.must(queryBuilder);
        }

        this.queryService.scrollSearch(methodInterface, admNoQueryBuilder, (String[])null, indices);
    }

    private Map<String, LinkedHashMap<String, Object>> getChildTypeHit(BoolQueryBuilder filterBuilder, QueryBuilder queryBuilder, String... indices) {
        Map<String, LinkedHashMap<String, Object>> curChildTypeHit = new HashMap();
        if (filterBuilder == null && queryBuilder == null) {
            return curChildTypeHit;
        } else {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            if (filterBuilder != null) {
                boolQueryBuilder.filter(filterBuilder);
            }

            boolQueryBuilder.filter(queryBuilder);
            long ss = System.currentTimeMillis();
            List<Map<String, Object>> allChildTypeList = this.queryService.scrollSearch(filterBuilder, indices);
            List<Map<String, Object>> allChildTypeHitList = this.queryService.scrollSearch(boolQueryBuilder, indices);
            List<String> diagnoseParentAdmNo = new ArrayList();

            Iterator var11;
            Map map;
            String curParentId;
            String curAdmId;
            LinkedHashMap curHash;
            String curAdmNo;
            for(var11 = allChildTypeHitList.iterator(); var11.hasNext(); curChildTypeHit.put(curParentId, curHash)) {
                map = (Map)var11.next();
                curParentId = map.getOrDefault("regno", "").toString();
                curAdmId = map.getOrDefault("admno", "").toString();
                curHash = (LinkedHashMap)curChildTypeHit.getOrDefault(curParentId, new LinkedHashMap());
                if (curHash.size() < 5) {
                    map.put("idHit", true);
                    curAdmNo = map.getOrDefault("doc_id", "").toString();
                    if (!StringUtils.isEmpty(curAdmNo)) {
                        diagnoseParentAdmNo.add(curAdmNo);
                    }

                    curHash.put(curAdmId, map);
                }
            }

            for(var11 = allChildTypeList.iterator(); var11.hasNext(); curChildTypeHit.put(curParentId, curHash)) {
                map = (Map)var11.next();
                curParentId = map.getOrDefault("regno", "").toString();
                curAdmId = map.getOrDefault("admno", "").toString();
                curHash = (LinkedHashMap)curChildTypeHit.get(curParentId);
                if (curHash == null) {
                    curHash = new LinkedHashMap();
                }

                Object m = curHash.get(curAdmId);
                if (m == null) {
                    map.put("idHit", false);
                    String curDocId = map.getOrDefault("doc_id", "").toString();
                    if (!StringUtils.isEmpty(curDocId) && curHash.size() < 5) {
                        diagnoseParentAdmNo.add(curDocId);
                    }

                    curHash.put(curAdmId, map);
                }
            }

            BoolQueryBuilder diagBoolQueryBuilder = new BoolQueryBuilder();
            Iterator var25 = diagnoseParentAdmNo.iterator();

            while(var25.hasNext()) {
                curParentId = (String)var25.next();
                diagBoolQueryBuilder.should(JoinQueryBuilders.parentId(ConfigProperties.getKey("es.query.table.diagnose"), curParentId));
            }

            List<Map<String, Object>> allDiagTypeList = diagnoseParentAdmNo.size() == 0 ? new ArrayList() : this.queryService.scrollSearch(diagBoolQueryBuilder, indices);
            Map<String, Map<String, String>> diag = new HashMap();

            Iterator var28;
            Map diagMap;
            for(var28 = ((List)allDiagTypeList).iterator(); var28.hasNext(); diag.put(curAdmNo, diagMap)) {
                Map<String, Object> hitMap = (Map)var28.next();
                curAdmNo = hitMap.getOrDefault("admno", "").toString();
                diagMap = (Map)diag.getOrDefault(curAdmNo, new HashMap());
                String curDiagType = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.diagnose.name"), "").toString();
                String curDiagName = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.diagnose.type"), "").toString();
                curDiagType = this.getDiagnoseType(curDiagType);
                String oldDiagName;
                if (!StringUtils.isEmpty(curDiagType) && !StringUtils.isEmpty(curDiagName)) {
                    oldDiagName = (String)diagMap.getOrDefault(curDiagType, "");
                    curDiagName = StringUtils.isEmpty(oldDiagName) ? curDiagName : oldDiagName + "," + curDiagName;
                    diagMap.put(curDiagType, curDiagName);
                } else {
                    oldDiagName = (String)diagMap.getOrDefault("其他诊断", "");
                    curDiagName = StringUtils.isEmpty(oldDiagName) ? curDiagName : oldDiagName + "," + curDiagName;
                    diagMap.put("其他诊断", curDiagName);
                }
            }

            var28 = curChildTypeHit.entrySet().iterator();

            while(var28.hasNext()) {
                Map.Entry<String, LinkedHashMap<String, Object>> entry = (Map.Entry)var28.next();
                curAdmNo = (String)entry.getKey();
                LinkedHashMap<String, Object> value = (LinkedHashMap)entry.getValue();
                int count = 0;
                Iterator var35 = value.entrySet().iterator();

                while(var35.hasNext()) {
                    Map.Entry<String, Object> centry = (Map.Entry)var35.next();
                    ++count;
                    if (count > 5) {
                        break;
                    }

                    String cKey = (String)centry.getKey();
                    Map<String, Object> cMap = (Map)centry.getValue();
                    Map<String, String> curD = (Map)diag.get(cKey);
                    if (curD != null) {
                        cMap.put(ConfigProperties.getKey("es.query.table.diagnose"), this.getDiagnoseStr(curD));
                    }
                }
            }

            return curChildTypeHit;
        }
    }

    private String getDiagnoseType(String curDiagType) {
        if (!StringUtils.isEmpty(curDiagType)) {
            if (!curDiagType.contains("门") && !curDiagType.contains("急")) {
                if (curDiagType.contains("入")) {
                    curDiagType = "入院诊断";
                } else if (curDiagType.contains("主")) {
                    curDiagType = "主要诊断";
                } else if (curDiagType.contains("出")) {
                    curDiagType = "出院诊断";
                } else {
                    curDiagType = "其他诊断";
                }
            } else {
                curDiagType = "门(急)诊诊断";
            }
        } else {
            curDiagType = "";
        }

        return curDiagType;
    }

    private String getDiagnoseStr(Map<String, String> diagMap) {
        if (diagMap == null) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            if (diagMap.get("入院诊断") != null) {
                sb.append("入院诊断-").append((String)diagMap.get("入院诊断")).append("; ");
            }

            if (diagMap.get("主要诊断") != null) {
                sb.append("主要诊断-").append((String)diagMap.get("主要诊断")).append("; ");
            }

            if (diagMap.get("出院诊断") != null) {
                sb.append("出院诊断-").append((String)diagMap.get("出院诊断")).append("; ");
            }

            if (diagMap.get("门(急)诊诊断") != null) {
                sb.append("门(急)诊诊断-").append((String)diagMap.get("门(急)诊诊断")).append("; ");
            }

            if (diagMap.get("其他诊断") != null) {
                sb.append("其他诊断-").append((String)diagMap.get("其他诊断")).append("; ");
            }

            return sb.toString();
        }
    }
}
