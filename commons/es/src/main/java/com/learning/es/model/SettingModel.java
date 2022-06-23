package com.learning.es.model;

public class SettingModel {
    private int shards = 5;
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
