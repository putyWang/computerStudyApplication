package com.learning.es.service.impl;

import com.learning.core.utils.CommonBeanUtil;
import com.learning.core.utils.StringUtils;
import com.learning.es.bean.*;
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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.join.query.HasParentQueryBuilder;
import org.elasticsearch.join.query.JoinQueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Log4j2
public class SearchServiceImpl<T extends Result>
        extends EsServiceImpl
        implements SearchService<T> {

    private final QueryService queryService;

    /**
     * 注入对应的es链接
     * @param restClientFactory es链接工厂类
     */
    public SearchServiceImpl(RestClientFactory restClientFactory) {
        super(restClientFactory);
        queryService = new QueryServiceImpl(restClientFactory);
    }

    public FulltextSearchResult<T> quickSearch(QuickConditionBuilder conditionBuilder, int page, int size, Class<T> clazz, String... indices) {
        //查询构造器
        QueryStringQueryBuilder queryBuilder = conditionBuilder
                .getQueryStringQueryBuilder();
        //高亮查询构造器
        HighlightBuilder highlightBuilder = conditionBuilder
                .getHighlightBuilder();
        //过滤查询构造器
        QueryBuilder filterQueryBuilder = conditionBuilder
                .getFilterQueryBuilder();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //1.构造查询条件
        int form = (page - 1) * size;

        if (filterQueryBuilder != null) {
            BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
            boolQueryBuilder
                    .must(queryBuilder)
                    .filter(filterQueryBuilder);
            searchSourceBuilder.query(boolQueryBuilder);
        } else {
            searchSourceBuilder.query(queryBuilder);
        }

        searchSourceBuilder
                .from(form)
                .size(size)
                .highlighter(highlightBuilder);

        log.info("全文检索查询条件 {}", searchSourceBuilder.toString());

        //2.获取搜索结果
        SearchHits searchHits = this
                .queryService
                .searchForHits(searchSourceBuilder, indices);

        //数据总条数
        long total = searchHits
                .getTotalHits()
                .value;
        List<Map<String, Object>> searchHitMaps = new ArrayList<>();
        Map<String, PropertyMapper> textMapper = conditionBuilder
                .getTextMapper();
        String queryKeys = conditionBuilder
                .getQueryString();
        String[] queryKeyList = queryKeys
                .split(" ");

        //3.设置搜索结果map以及高亮数据
        for (SearchHit hit : searchHits) {
            Map<String, Object> curMap = hit.getSourceAsMap();

            curMap.put("highlight", this.getHighlightStr(hit, textMapper, queryKeyList));
            searchHitMaps.add(curMap);
        }

        FulltextSearchResult<T> result = new FulltextSearchResult<>();
        result.setTotal(total);
        List<T> resultData = new ArrayList<>();


        //设置curR对象列表
        T curR;
        for(Iterator<Map<String, Object>> it = searchHitMaps.iterator(); it.hasNext(); resultData.add(curR)) {
            Map<String, Object> map = it.next();
            //创建T对象
            curR = CommonBeanUtil.getNewObject(clazz);
            //获取类中所有字段名
            Field[] declaredFields = clazz.getDeclaredFields();
            //对curR对象中属性赋值
            for (Field field : declaredFields) {
                try {
                    curR = setFieldValue(curR, field, map.get(field.getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //设置高亮值
            curR.setHighLight(map.getOrDefault("highlight", "").toString());
        }

        //设置查询结果
        result.setData(resultData);
        return result;
    }

//    public AdvancedSearchResult advancedSearch(ConditionBuilder conditionBuilder, int page, int size, String... indices) throws Exception {
//
//        QueryBuilder queryBuilder = conditionBuilder
//                .toQueryBuilder(1);
//        QueryBuilder querySecondBuilder = conditionBuilder
//                .toQueryBuilder(2);
//
//        int form = (page - 1) * size;
//        SearchResult searchResult = this.queryService.search(queryBuilder, size, form, indices);
//        long total = searchResult.getTotal();
//        List<Map<String, Object>> searchHitMaps = searchResult.getResultData();
//        Map<String, LinkedHashMap<String, Object>> curChildTypeHit = null;
//        if (searchHitMaps != null) {
//            List<String> regNos = new ArrayList<>();
//
//            for (Map<String, Object> hitMap : searchHitMaps) {
//                String curRegNo = hitMap
//                        .getOrDefault("regno", "")
//                        .toString();
//
//                regNos.add(curRegNo);
//            }
//
//            BoolQueryBuilder filterBuilder = new BoolQueryBuilder();
//            filterBuilder.filter(new TermsQueryBuilder("regno", regNos));
//            curChildTypeHit = this.getChildTypeHit(filterBuilder, querySecondBuilder, indices);
//        }
//
//        Map<String, PropertyMapper> mapperMap = conditionBuilder.getMapperMap();
//        AdvancedSearchResult result = new AdvancedSearchResult();
//        result.setTotal(total);
//        List<com.dhcc.mrp.elastic.bean.AdvancedSearchResult.Result> results = new ArrayList();
//        if (searchHitMaps == null) {
//            searchHitMaps = new ArrayList();
//        }
//
//        Iterator var36 = ((List) searchHitMaps).iterator();
//
//        while (var36.hasNext()) {
//            Map<String, Object> hitMap = (Map) var36.next();
//            com.dhcc.mrp.elastic.bean.AdvancedSearchResult.Result curR = new com.dhcc.mrp.elastic.bean.AdvancedSearchResult.Result();
//            String curRegNo = hitMap.getOrDefault("regno", "").toString();
//            String curPatientName = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.patient.name"), "").toString();
//            String curPatientGender = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.patient.gender"), "").toString();
//            String curBirthday = hitMap.getOrDefault(ConfigProperties.getKey("es.query.field.patient.birthday"), "").toString();
//            curR.setRegNo(curRegNo);
//            curR.setPatientName(curPatientName);
//            curR.setPatientGender(curPatientGender);
//            curR.setPatientBirthday(curBirthday);
//            LinkedHashMap<String, Object> curHash = curChildTypeHit != null ? (LinkedHashMap) curChildTypeHit.get(curRegNo) : null;
//            String dishDateField;
//            if (curHash == null) {
//                curR.setChildren((List) null);
//                curR.setChildTotal(0);
//            } else {
//                List<Child> childList = new ArrayList();
//                int count = 0;
//
//                Child child;
//                for (Iterator var26 = curHash.entrySet().iterator(); var26.hasNext(); childList.add(child)) {
//                    Map.Entry<String, Object> entry = (Map.Entry) var26.next();
//                    ++count;
//                    if (count > 5) {
//                        break;
//                    }
//
//                    HashMap<String, Object> admMap = (HashMap) entry.getValue();
//                    child = new Child();
//                    if (admMap != null && admMap.size() > 0) {
//                        child.setAdmNo(admMap.getOrDefault("admno", "").toString());
//                        child.setAdmType(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.type"), "").toString());
//                        child.setAdmDept(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.admdept"), "").toString());
//                        child.setAdmDate(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.admdate"), "").toString());
//                        dishDateField = ConfigProperties.getKey("es.query.field.adm.dishdate");
//                        String dishDeptField = ConfigProperties.getKey("es.query.field.adm.dishdept");
//                        if (admMap.get(dishDateField) != null) {
//                            child.setDishDate(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.dishdate"), "").toString());
//                        }
//
//                        if (admMap.get(dishDeptField) != null) {
//                            child.setDishDept(admMap.getOrDefault(ConfigProperties.getKey("es.query.field.adm.dishdept"), "").toString());
//                        }
//                    }
//                }
//
//                curR.setChildren(childList);
//                curR.setChildTotal(curHash.size());
//            }
//
//            Iterator var37 = hitMap.entrySet().iterator();
//
//            while (var37.hasNext()) {
//                Map.Entry<String, Object> entry = (Map.Entry) var37.next();
//                String key = (String) entry.getKey();
//                Object value = entry.getValue();
//                if (value instanceof String) {
//                    String valueStr = value.toString();
//                    int tuoMinType = mapperMap.get(key) == null ? 0 : ((PropertyMapper) mapperMap.get(key)).getTuoMinType();
//                    dishDateField = DesensitiseUtil.tuoMin(tuoMinType, valueStr);
//                    hitMap.put(key, dishDateField);
//                }
//            }
//
//            results.add(curR);
//        }
//
//        result.setData(results);
//        return result;
//    }

    /**
     * 将搜索结果转化为高亮结果
     * @param hit es 搜索结果
     * @param textMapper 搜索字段参数
     * @param queryKeyList 查询字段key
     * @return
     */
    private String getHighlightStr(SearchHit hit, Map<String, PropertyMapper> textMapper, String[] queryKeyList) {
        Collection<HighlightField> highlightFields = hit
                .getHighlightFields()
                .values();
        StringBuilder highlightSb = new StringBuilder();
        List<HighWeight> highWeights = new ArrayList<>();
        Iterator<HighlightField> it = highlightFields.iterator();

        while(true) {
            //当前处理的高亮字段名称
            String curProperty;
            //当前高亮片段
            StringBuilder curTextStr;
            //获取高亮片段
            do {
                //循环结束后 设置高亮字符串
                if (! it.hasNext()) {
                    //将highWeights数据按照权重由大到小进行排序
                    highWeights.sort((o1, o2) -> {
                        return o2.getWeight() - o1.getWeight();
                    });

                    for (HighWeight highWeight : highWeights) {
                        highlightSb
                                //设置高亮字段
                                .append(highWeight.getHighString())
                                //使用空格间隔
                                .append("&nbsp;");
                    }

                    return highlightSb.toString();
                }

                HighlightField highlightField = it.next();
                curProperty = highlightField.getName();
                Text[] text = highlightField.getFragments();
                curTextStr = new StringBuilder();

                //将高亮字段加入到curTextStr之中
                for(Text str: text) {
                    curTextStr.append(str.toString());
                }

            } while(! curTextStr
                    .toString()
                    .contains("</span>"));

            //中文描述
            String title = textMapper.get(curProperty) == null ? "" : textMapper.get(curProperty).getChineseDesc();
            //高亮字符串
            String curHigh;

            if (! StringUtils.isEmpty(title)) {
                curHigh = title + " - " + curTextStr.toString();
            } else {
                curHigh = curTextStr.toString();
            }

            //生成加权高亮结果
            HighWeight highWeight = new HighWeight();
            highWeight.setHighString(curHigh);
            int w = 0;

            //根据高亮字符串中包含搜索字符串的数量对高亮字符串加权
            for(String s : queryKeyList) {
                if (curHigh.contains(s)) {
                    ++w;
                }
            }

            highWeight.setWeight(w);
            highWeights.add(highWeight);
        }
    }

    /**
     * 为相应字段赋值
     * @param entity 实体对象
     * @param field 字段对象
     * @return
     * @throws IntrospectionException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private T setFieldValue(T entity, Field field, Object value)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        PropertyDescriptor propertyDescriptor =
                new PropertyDescriptor(field.getName(), entity.getClass());

        Method writeMethod = propertyDescriptor.getWriteMethod();

        return (T)writeMethod.invoke(entity, value);
    }
}
