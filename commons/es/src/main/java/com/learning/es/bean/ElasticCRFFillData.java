package com.learning.es.bean;

import java.util.Map;

public class ElasticCRFFillData {
    private String docId;
    private String fieldName;
    private Map<String, Object> fillData;

    public ElasticCRFFillData(String docId, String fieldName, Map<String, Object> fillData) {
        this.docId = docId;
        this.fieldName = fieldName;
        this.fillData = fillData;
    }

    public String getDocId() {
        return this.docId;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public Map<String, Object> getFillData() {
        return this.fillData;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFillData(Map<String, Object> fillData) {
        this.fillData = fillData;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ElasticCRFFillData)) {
            return false;
        } else {
            ElasticCRFFillData other = (ElasticCRFFillData)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label47: {
                    Object this$docId = this.getDocId();
                    Object other$docId = other.getDocId();
                    if (this$docId == null) {
                        if (other$docId == null) {
                            break label47;
                        }
                    } else if (this$docId.equals(other$docId)) {
                        break label47;
                    }

                    return false;
                }

                Object this$fieldName = this.getFieldName();
                Object other$fieldName = other.getFieldName();
                if (this$fieldName == null) {
                    if (other$fieldName != null) {
                        return false;
                    }
                } else if (!this$fieldName.equals(other$fieldName)) {
                    return false;
                }

                Object this$fillData = this.getFillData();
                Object other$fillData = other.getFillData();
                if (this$fillData == null) {
                    if (other$fillData != null) {
                        return false;
                    }
                } else if (!this$fillData.equals(other$fillData)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ElasticCRFFillData;
    }

    public int hashCode() {
        int result = 1;
        Object $docId = this.getDocId();
        result = result * 59 + ($docId == null ? 43 : $docId.hashCode());
        Object $fieldName = this.getFieldName();
        result = result * 59 + ($fieldName == null ? 43 : $fieldName.hashCode());
        Object $fillData = this.getFillData();
        result = result * 59 + ($fillData == null ? 43 : $fillData.hashCode());
        return result;
    }

    public String toString() {
        return "ElasticCRFFillData(docId=" + this.getDocId() + ", fieldName=" + this.getFieldName() + ", fillData=" + this.getFillData() + ")";
    }
}
