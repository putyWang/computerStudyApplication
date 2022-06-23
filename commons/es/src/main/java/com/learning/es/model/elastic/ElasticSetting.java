package com.learning.es.model.elastic;

import lombok.Data;

/**
 * es setting 配置
 * @author ：wangpenghui
 * @date ：Created in 2021/3/16 9:02
 */
@Data
public class ElasticSetting {
    /**
     * 副本数
     */
    private Integer replicasNumber = 0;
    /**
     * 分片数
     */
    private Integer shardNumber = 5;
    /**
     * 刷新时间间隔
     */
    private String refreshInterval = "1s";
    /**
     * 索引mapping最大字段数量
     */
    private Integer totalFieldLimit = 5000;

}
