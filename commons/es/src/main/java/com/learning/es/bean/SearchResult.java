package com.learning.es.bean;

import java.util.List;
import java.util.Map;

public class SearchResult {
    /**
     * 搜索结果
     */
    private List<Map<String, Object>> resultData;
    /**
     * 结果总条数
     */
    private long total;

    public SearchResult() {
    }

    public List<Map<String, Object>> getResultData() {
        return this.resultData;
    }

    public void setResultData(List<Map<String, Object>> resultData) {
        this.resultData = resultData;
    }

    public long getTotal() {
        return this.total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
