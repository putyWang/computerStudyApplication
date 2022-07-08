package com.learning.es.service;

import com.learning.es.bean.AdvancedSearchResult;
import com.learning.es.bean.FulltextSearchResult;
import com.learning.es.bean.Result;
import com.learning.es.bean.SearchResult;
import com.learning.es.constants.ElasticMethodInterface;
import com.learning.es.model.condition.ConditionBuilder;
import com.learning.es.model.QuickConditionBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import java.util.List;
import java.util.Map;

public interface SearchService<T extends Result>  {
    /**
     * 快速搜索
     * @param conditionBuilder 条件构造器
     * @param page 页
     * @param size 每页数据条数
     * @param indices 索引列表
     * @param clazz 泛型类
     * @return
     */
    FulltextSearchResult<T> quickSearch(QuickConditionBuilder conditionBuilder, int page, int size, Class<T> clazz, String... indices);

    /**
     * 高级搜索
     * @param conditionBuilder 条件构造器
     * @param page 页
     * @param size 每页数据条数
     * @param indices 索引列表
     * @return
     * @throws Exception
     */
//    AdvancedSearchResult advancedSearch(ConditionBuilder conditionBuilder, int page, int size, String... indices) throws Exception;
}
