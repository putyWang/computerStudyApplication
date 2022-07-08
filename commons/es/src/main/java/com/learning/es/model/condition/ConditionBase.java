package com.learning.es.model.condition;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

public class ConditionBase implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;
    private JSONObject query;

    public ConditionBase() {
    }

    public String getType() {
        return this.type;
    }

    public JSONObject getQuery() {
        return this.query;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setQuery(JSONObject query) {
        this.query = query;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ConditionBase)) {
            return false;
        } else {
            ConditionBase other = (ConditionBase)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$type = this.getType();
                Object other$type = other.getType();
                if (this$type == null) {
                    if (other$type != null) {
                        return false;
                    }
                } else if (!this$type.equals(other$type)) {
                    return false;
                }

                Object this$query = this.getQuery();
                Object other$query = other.getQuery();
                if (this$query == null) {
                    if (other$query != null) {
                        return false;
                    }
                } else if (!this$query.equals(other$query)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConditionBase;
    }

    public int hashCode() {
        int result = 1;
        Object $type = this.getType();
        result = result * 59 + ($type == null ? 43 : $type.hashCode());
        Object $query = this.getQuery();
        result = result * 59 + ($query == null ? 43 : $query.hashCode());
        return result;
    }

    public String toString() {
        return "ConditionBase(type=" + this.getType() + ", query=" + this.getQuery() + ")";
    }
}
