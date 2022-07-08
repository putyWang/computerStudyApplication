package com.learning.es.utils.tree;

import java.io.Serializable;
import java.util.List;

public class TreeNode<T> implements Serializable {
    private static final long serialVersionUID = -9189631784252440402L;
    private Integer id;
    private Integer pid;
    private String rowKey;
    private String name;
    public Boolean leaf = true;
    public Boolean expanded = false;
    public T data;
    private List<TreeNode<T>> children;

    public TreeNode(Integer id, Integer pid, String name, T data) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.data = data;
        this.rowKey = pid + "_" + id;
    }

    public TreeNode(Integer id, Integer pid, String name, T data, List<TreeNode<T>> children) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.data = data;
        this.rowKey = pid + "_" + id;
        this.children = children;
    }

    public Integer getId() {
        return this.id;
    }

    public Integer getPid() {
        return this.pid;
    }

    public String getRowKey() {
        return this.rowKey;
    }

    public String getName() {
        return this.name;
    }

    public Boolean getLeaf() {
        return this.leaf;
    }

    public Boolean getExpanded() {
        return this.expanded;
    }

    public T getData() {
        return this.data;
    }

    public List<TreeNode<T>> getChildren() {
        return this.children;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLeaf(Boolean leaf) {
        this.leaf = leaf;
    }

    public void setExpanded(Boolean expanded) {
        this.expanded = expanded;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof TreeNode)) {
            return false;
        } else {
            TreeNode<?> other = (TreeNode)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label107: {
                    Object this$id = this.getId();
                    Object other$id = other.getId();
                    if (this$id == null) {
                        if (other$id == null) {
                            break label107;
                        }
                    } else if (this$id.equals(other$id)) {
                        break label107;
                    }

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

                Object this$rowKey = this.getRowKey();
                Object other$rowKey = other.getRowKey();
                if (this$rowKey == null) {
                    if (other$rowKey != null) {
                        return false;
                    }
                } else if (!this$rowKey.equals(other$rowKey)) {
                    return false;
                }

                label86: {
                    Object this$name = this.getName();
                    Object other$name = other.getName();
                    if (this$name == null) {
                        if (other$name == null) {
                            break label86;
                        }
                    } else if (this$name.equals(other$name)) {
                        break label86;
                    }

                    return false;
                }

                label79: {
                    Object this$leaf = this.getLeaf();
                    Object other$leaf = other.getLeaf();
                    if (this$leaf == null) {
                        if (other$leaf == null) {
                            break label79;
                        }
                    } else if (this$leaf.equals(other$leaf)) {
                        break label79;
                    }

                    return false;
                }

                label72: {
                    Object this$expanded = this.getExpanded();
                    Object other$expanded = other.getExpanded();
                    if (this$expanded == null) {
                        if (other$expanded == null) {
                            break label72;
                        }
                    } else if (this$expanded.equals(other$expanded)) {
                        break label72;
                    }

                    return false;
                }

                Object this$data = this.getData();
                Object other$data = other.getData();
                if (this$data == null) {
                    if (other$data != null) {
                        return false;
                    }
                } else if (!this$data.equals(other$data)) {
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
        return other instanceof TreeNode;
    }

    public int hashCode() {
        int PRIME = true;
        int result = 1;
        Object $id = this.getId();
        int result = result * 59 + ($id == null ? 43 : $id.hashCode());
        Object $pid = this.getPid();
        result = result * 59 + ($pid == null ? 43 : $pid.hashCode());
        Object $rowKey = this.getRowKey();
        result = result * 59 + ($rowKey == null ? 43 : $rowKey.hashCode());
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $leaf = this.getLeaf();
        result = result * 59 + ($leaf == null ? 43 : $leaf.hashCode());
        Object $expanded = this.getExpanded();
        result = result * 59 + ($expanded == null ? 43 : $expanded.hashCode());
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        Object $children = this.getChildren();
        result = result * 59 + ($children == null ? 43 : $children.hashCode());
        return result;
    }

    public String toString() {
        return "TreeNode(id=" + this.getId() + ", pid=" + this.getPid() + ", rowKey=" + this.getRowKey() + ", name=" + this.getName() + ", leaf=" + this.getLeaf() + ", expanded=" + this.getExpanded() + ", data=" + this.getData() + ", children=" + this.getChildren() + ")";
    }
}

