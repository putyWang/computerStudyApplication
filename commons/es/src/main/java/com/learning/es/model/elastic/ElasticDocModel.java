package com.learning.es.model.elastic;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * canal数据转es数据 模板类
 * @author wangpenghui
 * @createTime 2021年08月13日 09:57:00
 */
@Data
public class ElasticDocModel {
    /**
     * es索引名称
     */
    private String index;
    /**
     * es type
     */
    private String docType = "doc";
    /**
     * 操作事件类型,新增、修改或删除
     */
    private CanalEntry.EventType eventTypeEnum;
    /**
     * es数据
     */
    private List<Map<String, Object>> docList;

}
