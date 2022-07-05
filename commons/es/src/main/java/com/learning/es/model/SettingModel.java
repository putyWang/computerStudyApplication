package com.learning.es.model;

/**
 * 索引设置
 */
public class SettingModel {
    /**
     * 主分片数
     */
    private int shards = 5;

    /**
     * 每个主分片的副本数
     */
    private int replicas = 1;

    public SettingModel() {
    }

    public int getShards() {
        return this.shards;
    }

    public void setShards(int shards) {
        this.shards = shards;
    }

    public int getReplicas() {
        return this.replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }
}
