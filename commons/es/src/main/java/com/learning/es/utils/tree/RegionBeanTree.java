package com.learning.es.utils.tree;

import java.io.Serializable;
import java.util.List;

public class RegionBeanTree
        implements Serializable {
    private Object id;
    private String code;
    private Object pid;
    private String label;
    private Object data;
    private List<RegionBeanTree> children;

    public RegionBeanTree() {
    }

    public Object getId() {
        return this.id;
    }

    public String getCode() {
        return this.code;
    }

    public Object getPid() {
        return this.pid;
    }

    public String getLabel() {
        return this.label;
    }

    public Object getData() {
        return this.data;
    }

    public List<RegionBeanTree> getChildren() {
        return this.children;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setPid(Object pid) {
        this.pid = pid;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setChildren(List<RegionBeanTree> children) {
        this.children = children;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RegionBeanTree)) {
            return false;
        } else {
            RegionBeanTree other = (RegionBeanTree)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$id = this.getId();
                Object other$id = other.getId();
                if (this$id == null) {
                    if (other$id != null) {
                        return false;
                    }
                } else if (!this$id.equals(other$id)) {
                    return false;
                }

                Object this$code = this.getCode();
                Object other$code = other.getCode();
                if (this$code == null) {
                    if (other$code != null) {
                        return false;
                    }
                } else if (!this$code.equals(other$code)) {
                    return false;
                }

                Object this$pid = this.getPid();
                Object other$pid = other.getPid();
                if (this$pid == null) {
                    if (other$pid != null) {
                        return false;
                    }
                } else if (!this$pid.equals(other$pid)) {
                    return false;
                }

                label62: {
                    Object this$label = this.getLabel();
                    Object other$label = other.getLabel();
                    if (this$label == null) {
                        if (other$label == null) {
                            break label62;
                        }
                    } else if (this$label.equals(other$label)) {
                        break label62;
                    }

                    return false;
                }

                label55: {
                    Object this$data = this.getData();
                    Object other$data = other.getData();
                    if (this$data == null) {
                        if (other$data == null) {
                            break label55;
                        }
                    } else if (this$data.equals(other$data)) {
                        break label55;
                    }

                    return false;
                }

                Object this$children = this.getChildren();
                Object other$children = other.getChildren();
                if (this$children == null) {
                    if (other$children != null) {
                        return false;
                    }
                } else if (!this$children.equals(other$children)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof RegionBeanTree;
    }

    public int hashCode() {
        int result = 1;
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $code = this.getCode();
        result = result * 59 + ($code == null ? 43 : $code.hashCode());
        Object $pid = this.getPid();
        result = result * 59 + ($pid == null ? 43 : $pid.hashCode());
        Object $label = this.getLabel();
        result = result * 59 + ($label == null ? 43 : $label.hashCode());
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        Object $children = this.getChildren();
        result = result * 59 + ($children == null ? 43 : $children.hashCode());
        return result;
    }

    public String toString() {
        return "RegionBeanTree(id=" + this.getId() + ", code=" + this.getCode() + ", pid=" + this.getPid() + ", label=" + this.getLabel() + ", data=" + this.getData() + ", children=" + this.getChildren() + ")";
    }
}
