package com.learning.es.bean;

/**
 * 本地索引状态类
 */
public class IndicesStatusLocal {
    private String uuid;
    private String indexName;
    private int primary;
    private int replicas;
    private long countDocs;
    private long deletedDocs;
    private String totalStoreSize;
    private String primaryStoreSize;
    private String health;

    public IndicesStatusLocal() {
    }

    public String getUuid() {
        return this.uuid;
    }

    public String getIndexName() {
        return this.indexName;
    }

    public int getPrimary() {
        return this.primary;
    }

    public int getReplicas() {
        return this.replicas;
    }

    public long getCountDocs() {
        return this.countDocs;
    }

    public long getDeletedDocs() {
        return this.deletedDocs;
    }

    public String getTotalStoreSize() {
        return this.totalStoreSize;
    }

    public String getPrimaryStoreSize() {
        return this.primaryStoreSize;
    }

    public String getHealth() {
        return this.health;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public void setPrimary(int primary) {
        this.primary = primary;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public void setCountDocs(long countDocs) {
        this.countDocs = countDocs;
    }

    public void setDeletedDocs(long deletedDocs) {
        this.deletedDocs = deletedDocs;
    }

    public void setTotalStoreSize(String totalStoreSize) {
        this.totalStoreSize = totalStoreSize;
    }

    public void setPrimaryStoreSize(String primaryStoreSize) {
        this.primaryStoreSize = primaryStoreSize;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof IndicesStatusLocal)) {
            return false;
        } else {
            IndicesStatusLocal other = (IndicesStatusLocal)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label87: {
                    Object this$uuid = this.getUuid();
                    Object other$uuid = other.getUuid();
                    if (this$uuid == null) {
                        if (other$uuid == null) {
                            break label87;
                        }
                    } else if (this$uuid.equals(other$uuid)) {
                        break label87;
                    }

                    return false;
                }

                Object this$indexName = this.getIndexName();
                Object other$indexName = other.getIndexName();
                if (this$indexName == null) {
                    if (other$indexName != null) {
                        return false;
                    }
                } else if (!this$indexName.equals(other$indexName)) {
                    return false;
                }

                if (this.getPrimary() != other.getPrimary()) {
                    return false;
                } else if (this.getReplicas() != other.getReplicas()) {
                    return false;
                } else if (this.getCountDocs() != other.getCountDocs()) {
                    return false;
                } else if (this.getDeletedDocs() != other.getDeletedDocs()) {
                    return false;
                } else {
                    label67: {
                        Object this$totalStoreSize = this.getTotalStoreSize();
                        Object other$totalStoreSize = other.getTotalStoreSize();
                        if (this$totalStoreSize == null) {
                            if (other$totalStoreSize == null) {
                                break label67;
                            }
                        } else if (this$totalStoreSize.equals(other$totalStoreSize)) {
                            break label67;
                        }

                        return false;
                    }

                    Object this$primaryStoreSize = this.getPrimaryStoreSize();
                    Object other$primaryStoreSize = other.getPrimaryStoreSize();
                    if (this$primaryStoreSize == null) {
                        if (other$primaryStoreSize != null) {
                            return false;
                        }
                    } else if (!this$primaryStoreSize.equals(other$primaryStoreSize)) {
                        return false;
                    }

                    Object this$health = this.getHealth();
                    Object other$health = other.getHealth();
                    if (this$health == null) {
                        if (other$health != null) {
                            return false;
                        }
                    } else if (!this$health.equals(other$health)) {
                        return false;
                    }

                    return true;
                }
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof IndicesStatusLocal;
    }

    public int hashCode() {
        int result = 1;
        Object $uuid = this.getUuid();
        result = result * 59 + ($uuid == null ? 43 : $uuid.hashCode());
        Object $indexName = this.getIndexName();
        result = result * 59 + ($indexName == null ? 43 : $indexName.hashCode());
        result = result * 59 + this.getPrimary();
        result = result * 59 + this.getReplicas();
        long $countDocs = this.getCountDocs();
        result = result * 59 + (int)($countDocs >>> 32 ^ $countDocs);
        long $deletedDocs = this.getDeletedDocs();
        result = result * 59 + (int)($deletedDocs >>> 32 ^ $deletedDocs);
        Object $totalStoreSize = this.getTotalStoreSize();
        result = result * 59 + ($totalStoreSize == null ? 43 : $totalStoreSize.hashCode());
        Object $primaryStoreSize = this.getPrimaryStoreSize();
        result = result * 59 + ($primaryStoreSize == null ? 43 : $primaryStoreSize.hashCode());
        Object $health = this.getHealth();
        result = result * 59 + ($health == null ? 43 : $health.hashCode());
        return result;
    }

    public String toString() {
        return "IndicesStatusLocal(uuid=" + this.getUuid() + ", indexName=" + this.getIndexName() + ", primary=" + this.getPrimary() + ", replicas=" + this.getReplicas() + ", countDocs=" + this.getCountDocs() + ", deletedDocs=" + this.getDeletedDocs() + ", totalStoreSize=" + this.getTotalStoreSize() + ", primaryStoreSize=" + this.getPrimaryStoreSize() + ", health=" + this.getHealth() + ")";
    }
}
