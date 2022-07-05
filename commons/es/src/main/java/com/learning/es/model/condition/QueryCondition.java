package com.learning.es.model.condition;

import com.learning.es.model.FieldItemInfo;

/**
 * 查询条件组
 */
public final class QueryCondition
        extends QueryBase {

    /**
     * 关联词
     */
    private String relative;
    /**
     * 字段信息
     */
    private FieldItemInfo fieldItemInfo;
    /**
     * 其余字段信息
     */
    private FieldItemInfo otherFieldItemInfo;
    /**
     * 下限值或者精确值
     */
    private Object value;
    /**
     * 上限值
     */
    private Object endValue;
    private Object otherValue;
    private String condition;
    private String compare;
    private String searchItemName;

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof QueryCondition)) {
            return false;
        } else {
            QueryCondition other = (QueryCondition)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!super.equals(o)) {
                return false;
            } else {
                label121: {
                    Object this$relative = this.getRelative();
                    Object other$relative = other.getRelative();
                    if (this$relative == null) {
                        if (other$relative == null) {
                            break label121;
                        }
                    } else if (this$relative.equals(other$relative)) {
                        break label121;
                    }

                    return false;
                }

                Object this$fieldItemInfo = this.getFieldItemInfo();
                Object other$fieldItemInfo = other.getFieldItemInfo();
                if (this$fieldItemInfo == null) {
                    if (other$fieldItemInfo != null) {
                        return false;
                    }
                } else if (!this$fieldItemInfo.equals(other$fieldItemInfo)) {
                    return false;
                }

                label107: {
                    Object this$otherFieldItemInfo = this.getOtherFieldItemInfo();
                    Object other$otherFieldItemInfo = other.getOtherFieldItemInfo();
                    if (this$otherFieldItemInfo == null) {
                        if (other$otherFieldItemInfo == null) {
                            break label107;
                        }
                    } else if (this$otherFieldItemInfo.equals(other$otherFieldItemInfo)) {
                        break label107;
                    }

                    return false;
                }

                Object this$value = this.getValue();
                Object other$value = other.getValue();
                if (this$value == null) {
                    if (other$value != null) {
                        return false;
                    }
                } else if (!this$value.equals(other$value)) {
                    return false;
                }

                Object this$endValue = this.getEndValue();
                Object other$endValue = other.getEndValue();
                if (this$endValue == null) {
                    if (other$endValue != null) {
                        return false;
                    }
                } else if (!this$endValue.equals(other$endValue)) {
                    return false;
                }

                label86: {
                    Object this$otherValue = this.getOtherValue();
                    Object other$otherValue = other.getOtherValue();
                    if (this$otherValue == null) {
                        if (other$otherValue == null) {
                            break label86;
                        }
                    } else if (this$otherValue.equals(other$otherValue)) {
                        break label86;
                    }

                    return false;
                }

                label79: {
                    Object this$condition = this.getCondition();
                    Object other$condition = other.getCondition();
                    if (this$condition == null) {
                        if (other$condition == null) {
                            break label79;
                        }
                    } else if (this$condition.equals(other$condition)) {
                        break label79;
                    }

                    return false;
                }

                Object this$compare = this.getCompare();
                Object other$compare = other.getCompare();
                if (this$compare == null) {
                    if (other$compare != null) {
                        return false;
                    }
                } else if (!this$compare.equals(other$compare)) {
                    return false;
                }

                Object this$searchItemName = this.getSearchItemName();
                Object other$searchItemName = other.getSearchItemName();
                if (this$searchItemName == null) {
                    if (other$searchItemName != null) {
                        return false;
                    }
                } else if (!this$searchItemName.equals(other$searchItemName)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof QueryCondition;
    }

    public int hashCode() {
        int result = super.hashCode();
        Object $relative = this.getRelative();
        result = result * 59 + ($relative == null ? 43 : $relative.hashCode());
        Object $fieldItemInfo = this.getFieldItemInfo();
        result = result * 59 + ($fieldItemInfo == null ? 43 : $fieldItemInfo.hashCode());
        Object $otherFieldItemInfo = this.getOtherFieldItemInfo();
        result = result * 59 + ($otherFieldItemInfo == null ? 43 : $otherFieldItemInfo.hashCode());
        Object $value = this.getValue();
        result = result * 59 + ($value == null ? 43 : $value.hashCode());
        Object $endValue = this.getEndValue();
        result = result * 59 + ($endValue == null ? 43 : $endValue.hashCode());
        Object $otherValue = this.getOtherValue();
        result = result * 59 + ($otherValue == null ? 43 : $otherValue.hashCode());
        Object $condition = this.getCondition();
        result = result * 59 + ($condition == null ? 43 : $condition.hashCode());
        Object $compare = this.getCompare();
        result = result * 59 + ($compare == null ? 43 : $compare.hashCode());
        Object $searchItemName = this.getSearchItemName();
        result = result * 59 + ($searchItemName == null ? 43 : $searchItemName.hashCode());
        return result;
    }

    public QueryCondition() {
    }

    public String getRelative() {
        return this.relative;
    }

    public FieldItemInfo getFieldItemInfo() {
        return this.fieldItemInfo;
    }

    public FieldItemInfo getOtherFieldItemInfo() {
        return this.otherFieldItemInfo;
    }

    public Object getValue() {
        return this.value;
    }

    public Object getEndValue() {
        return this.endValue;
    }

    public Object getOtherValue() {
        return this.otherValue;
    }

    public String getCondition() {
        return this.condition;
    }

    public String getCompare() {
        return this.compare;
    }

    public String getSearchItemName() {
        return this.searchItemName;
    }

    public void setRelative(String relative) {
        this.relative = relative;
    }

    public void setFieldItemInfo(FieldItemInfo fieldItemInfo) {
        this.fieldItemInfo = fieldItemInfo;
    }

    public void setOtherFieldItemInfo(FieldItemInfo otherFieldItemInfo) {
        this.otherFieldItemInfo = otherFieldItemInfo;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setEndValue(Object endValue) {
        this.endValue = endValue;
    }

    public void setOtherValue(Object otherValue) {
        this.otherValue = otherValue;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setCompare(String compare) {
        this.compare = compare;
    }

    public void setSearchItemName(String searchItemName) {
        this.searchItemName = searchItemName;
    }

    public String toString() {
        return "QueryCondition(relative=" + this.getRelative() + ", fieldItemInfo=" + this.getFieldItemInfo() + ", otherFieldItemInfo=" + this.getOtherFieldItemInfo() + ", value=" + this.getValue() + ", endValue=" + this.getEndValue() + ", otherValue=" + this.getOtherValue() + ", condition=" + this.getCondition() + ", compare=" + this.getCompare() + ", searchItemName=" + this.getSearchItemName() + ")";
    }
}
