package com.learning.es.service;

import com.learning.es.bean.FulltextSearchResult;
import com.learning.es.bean.Result;
import com.learning.es.model.QuickConditionBuilder;

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
}
