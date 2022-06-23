package com.learning.es.model;

import com.learning.core.utils.StringUtils;
import lombok.Data;

/**
 * 前端查询条件传参
 * @author wangpenghui
 * @since 2021-03-18
 */
@Data
public class QueryJson {
    /**
     * 高级检索条件
     */
    private String queryJson;
    /**
     * 纳入条件
     */
    private String bringIntoCondJson;
    /**
     * 排除条件
     */
    private String rulingOutCondJson;
    /**
     * 页码数
     */
    private Integer page;
    /**
     * 每页显示数量
     */
    private Integer size;

    public boolean isEmpty(){
        return StringUtils.isEmpty(this.queryJson)
                && StringUtils.isEmpty(this.bringIntoCondJson)
                && StringUtils.isEmpty(this.rulingOutCondJson);
    }

}
