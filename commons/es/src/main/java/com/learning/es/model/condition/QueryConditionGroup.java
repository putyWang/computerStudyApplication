package com.learning.es.model.condition;

import java.util.ArrayList;
import java.util.List;

public class QueryConditionGroup extends QueryBase {
    private String logical = "AND";
    private List<ConditionBase> children = new ArrayList<>();

    public String getLogical() {
        return this.logical;
    }

    public void setLogical(String logical) {
        this.logical = logical;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof QueryConditionGroup)) {
            return false;
        } else {
            QueryConditionGroup other = (QueryConditionGroup)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                Object this$logical = this.getLogical();
                Object other$logical = other.getLogical();
                if (this$logical == null) {
                    if (other$logical != null) {
                        return false;
                    }
                } else if (!this$logical.equals(other$logical)) {
                    return false;
                }

                Object this$children = this.getChildren();
                Object other$children = other.getChildren();
                if (this$children == null) {

                    return other$children == null;
                } else

                    return this$children.equals(other$children);
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof QueryConditionGroup;
    }

    public int hashCode() {
        int result = super.hashCode();
        Object $logical = this.getLogical();
        result = result * 59 + ($logical == null ? 43 : $logical.hashCode());
        Object $children = this.getChildren();
        result = result * 59 + ($children == null ? 43 : $children.hashCode());
        return result;
    }

    public QueryConditionGroup() {
    }

    public List<ConditionBase> getChildren() {
        return this.children;
    }

    public void setChildren(List<ConditionBase> children) {
        this.children = children;
    }

    public String toString() {
        return "QueryConditionGroup(logical=" + this.getLogical() + ", children=" + this.getChildren() + ")";
    }
}
