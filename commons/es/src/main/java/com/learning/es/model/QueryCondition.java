package com.learning.es.model;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;

/**
 * 查询条件组
 * @author wangpenghui
 */
@Data
public class QueryCondition implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;

    private JSONObject query;
}
