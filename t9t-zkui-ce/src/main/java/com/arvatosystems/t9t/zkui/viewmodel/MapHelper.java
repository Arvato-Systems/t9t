package com.arvatosystems.t9t.zkui.viewmodel;

public class MapHelper<KEY, VALUE> {

    private KEY key;
    private VALUE value;

    public MapHelper(KEY key, VALUE value) {
        this.key = key;
        this.value = value;
    }

    public KEY getKey() {
        return key;
    }

    public void setKey(KEY key) {
        this.key = key;
    }

    public VALUE getValue() {
        return value;
    }

    public void setValue(VALUE value) {
        this.value = value;
    }
}
