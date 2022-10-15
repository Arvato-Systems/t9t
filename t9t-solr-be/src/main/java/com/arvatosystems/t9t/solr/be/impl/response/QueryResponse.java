package com.arvatosystems.t9t.solr.be.impl.response;

import java.util.ArrayList;

public class QueryResponse {
    private ResponseHeader responseHeader;
    private Response response;
    private long elapsedTime;

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getQTime() {
        return responseHeader.getQTime();
    }

    public ArrayList<Object> getResults() {
        return response.getDocs();
    }

    public long getElapsedTime() {
        return elapsedTime;
    }
}
