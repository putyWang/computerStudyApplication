package com.learning.es.model.condition;

import com.learning.es.enums.ESFieldTypeEnum;

import java.io.Serializable;

/**
 * 字段参数类
 */
public class PropertyMapper implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 参数
     */
    private String property;
    /**
     * 字段类型
     */
    private ESFieldTypeEnum propertyType;
    /**
     * 中文描述
     */
    private String chineseDesc;
    /**
     * 脱敏类型
     */
    private Integer tuoMinType;

    public PropertyMapper() {
    }

    public String getProperty() {
        return this.property;
    }

    public ESFieldTypeEnum getPropertyType() {
        return this.propertyType;
    }

    public String getChineseDesc() {
        return this.chineseDesc;
    }

    public Integer getTuoMinType() {
        return this.tuoMinType;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setPropertyType(ESFieldTypeEnum propertyType) {
        this.propertyType = propertyType;
    }

    public void setChineseDesc(String chineseDesc) {
        this.chineseDesc = chineseDesc;
    }

    public void setTuoMinType(Integer tuoMinType) {
        this.tuoMinType = tuoMinType;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof PropertyMapper)) {
            return false;
        } else {
            PropertyMapper other = (PropertyMapper)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label59: {
                    Object this$property = this.getProperty();
                    Object other$property = other.getProperty();
                    if (this$property == null) {
                        if (other$property == null) {
                            break label59;
                        }
                    } else if (this$property.equals(other$property)) {
                        break label59;
                    }

                    return false;
                }

                Object this$propertyType = this.getPropertyType();
                Object other$propertyType = other.getPropertyType();
                if (this$propertyType == null) {
                    if (other$propertyType != null) {
                        return false;
                    }
                } else if (!this$propertyType.equals(other$propertyType)) {
                    return false;
                }

                Object this$chineseDesc = this.getChineseDesc();
                Object other$chineseDesc = other.getChineseDesc();
                if (this$chineseDesc == null) {
                    if (other$chineseDesc != null) {
                        return false;
                    }
                } else if (!this$chineseDesc.equals(other$chineseDesc)) {
                    return false;
                }

                Object this$tuoMinType = this.getTuoMinType();
                Object other$tuoMinType = other.getTuoMinType();
                if (this$tuoMinType == null) {
                    if (other$tuoMinType != null) {
                        return false;
                    }
                } else if (!this$tuoMinType.equals(other$tuoMinType)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof PropertyMapper;
    }

    public int hashCode() {
        int result = 1;
        Object $property = this.getProperty();
        result = result * 59 + ($property == null ? 43 : $property.hashCode());
        ESFieldTypeEnum $propertyType = this.getPropertyType();
        result = result * 59 + ($propertyType == null ? 43 : $propertyType.hashCode());
        Object $chineseDesc = this.getChineseDesc();
        result = result * 59 + ($chineseDesc == null ? 43 : $chineseDesc.hashCode());
        Object $tuoMinType = this.getTuoMinType();
        result = result * 59 + ($tuoMinType == null ? 43 : $tuoMinType.hashCode());
        return result;
    }

    public String toString() {
        return "PropertyMapper(property=" + this.getProperty() + ", propertyType=" + this.getPropertyType() + ", chineseDesc=" + this.getChineseDesc() + ", tuoMinType=" + this.getTuoMinType() + ")";
    }
}
