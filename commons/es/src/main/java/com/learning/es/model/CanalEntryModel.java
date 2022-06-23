package com.learning.es.model;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * canal entry模板类
 * @author wangpenghui
 * @createTime 2021年08月13日 09:57:00
 */
@Data
public class CanalEntryModel {
    /**
     * 数据库名
     */
    private String schemaName;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 操作事件类型,新增、修改或删除
     */
    private CanalEntry.EventType eventTypeEnum;
    /**
     * 数据，结构为<列名，列值>
     */
    private List<Map<String, Object>> datas;

}
