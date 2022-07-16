package com.learning.es.service.impl;

import com.learning.core.utils.CommonBeanUtil;
import com.learning.core.utils.StringUtils;
import com.learning.es.bean.*;
import com.learning.es.clients.RestClientFactory;
import com.learning.es.model.QuickConditionBuilder;
import com.learning.es.model.condition.PropertyMapper;
import com.learning.es.service.EsServiceImpl;
import com.learning.es.service.QueryService;
import com.learning.es.service.SearchService;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
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
