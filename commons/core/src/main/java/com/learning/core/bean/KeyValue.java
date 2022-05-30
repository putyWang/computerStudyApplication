package com.learning.core.bean;

import java.io.Serializable;

public class KeyValue implements Serializable {

    private Object id;
    private String text;
    private Object extend;

    public KeyValue() {
    }

    public KeyValue(Object id, String text) {
        this.id = id;
        this.text = text;
    }

    public KeyValue(Object id, String text, Object extend) {
        this.id = id;
        this.text = text;
        this.extend = extend;
    }

    public Object getId() {
        return this.id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getExtend() {
        return this.extend;
    }

    public void setExtend(Object extend) {
        this.extend = extend;
    }
}
