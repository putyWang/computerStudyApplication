package com.learning.es.model.condition;

import java.io.Serializable;

/**
 *
 */
public final class FilterQuery
        implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 字段名称
     */
    private String field;
    /**
     * 字段类型
     */
    private String type;
    /**
     * 范围查询起始值
     * 精确查询的值
     */
    private String start;
    /**
     * 范围查询终点值
     */
    private String end;
    /**
     * 查询类型
     */
    private String queryType;

    public FilterQuery() {
    }

    public String getField() {
        return this.field;
    }

    public String getType() {
        return this.type;
    }

    public String getStart() {
        return this.start;
    }

    public String getEnd() {
        return this.end;
    }

    public String getQueryType() {
        return this.queryType;
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FilterQuery)) {
            return false;
        } else {
            FilterQuery other = (FilterQuery)o;
            Object this$field = this.getField();
            Object other$field = other.getField();
            if (this$field == null) {
                if (other$field != null) {
                    return false;
                }
            } else if (!this$field.equals(other$field)) {
                return false;
            }

            label61: {
                Object this$type = this.getType();
                Object other$type = other.getType();
                if (this$type == null) {
                    if (other$type == null) {
                        break label61;
                    }
                } else if (this$type.equals(other$type)) {
                    break label61;
                }

                return false;
            }

            label54: {
                Object this$start = this.getStart();
                Object other$start = other.getStart();
                if (this$start == null) {
                    if (other$start == null) {
                        break label54;
                    }
                } else if (this$start.equals(other$start)) {
                    break label54;
                }

                return false;
            }

            Object this$end = this.getEnd();
            Object other$end = other.getEnd();
            if (this$end == null) {
                if (other$end != null) {
                    return false;
                }
            } else if (!this$end.equals(other$end)) {
                return false;
            }

            Object this$queryType = this.getQueryType();
            Object other$queryType = other.getQueryType();
            if (this$queryType == null) {
                if (other$queryType != null) {
                    return false;
                }
            } else if (!this$queryType.equals(other$queryType)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        int result = 1;
        Object $field = this.getField();
        result = result * 59 + ($field == null ? 43 : $field.hashCode());
        Object $type = this.getType();
        result = result * 59 + ($type == null ? 43 : $type.hashCode());
        Object $start = this.getStart();
        result = result * 59 + ($start == null ? 43 : $start.hashCode());
        Object $end = this.getEnd();
        result = result * 59 + ($end == null ? 43 : $end.hashCode());
        Object $queryType = this.getQueryType();
        result = result * 59 + ($queryType == null ? 43 : $queryType.hashCode());
        return result;
    }

    public String toString() {
        return "FilterQuery(field=" + this.getField() + ", type=" + this.getType() + ", start=" + this.getStart() + ", end=" + this.getEnd() + ", queryType=" + this.getQueryType() + ")";
    }
}
