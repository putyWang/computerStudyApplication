package com.learning.es.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 全文检索结果
 */
public class FulltextSearchResult<T extends Result> {
    /**
     * 数据总条数
     */
    private long total = 0L;
    /**
     * 数据数组
     */
    private List<T> data = new ArrayList<>();

    public FulltextSearchResult() {
    }

    public long getTotal() {
        return this.total;
    }

    public List<T> getData() {
        return this.data;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FulltextSearchResult)) {
            return false;
        } else {
            FulltextSearchResult other = (FulltextSearchResult)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getTotal() != other.getTotal()) {
                return false;
            } else {
                Object this$data = this.getData();
                Object other$data = other.getData();
                if (this$data == null) {
                    if (other$data != null) {
                        return false;
                    }
                } else if (!this$data.equals(other$data)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FulltextSearchResult;
    }

    public int hashCode() {
        int result = 1;
        long $total = this.getTotal();
        result = result * 59 + (int)($total >>> 32 ^ $total);
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "FulltextSearchResult(total=" + this.getTotal() + ", data=" + this.getData() + ")";
    }
}
