package com.arvatosystems.t9t.solr.be.impl.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

public class ResponseHeader {
    private int status;
    private int qTime;
    private Params params;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @JsonGetter("QTime")
    public int getQTime() {
        return qTime;
    }

    @JsonSetter("QTime")
    public void setQTime(int time) {
        qTime = time;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }
}
