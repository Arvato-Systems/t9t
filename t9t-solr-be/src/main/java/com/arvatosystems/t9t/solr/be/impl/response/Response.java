package com.arvatosystems.t9t.solr.be.impl.response;

import java.util.ArrayList;

public class Response {
    private int numFound;
    private int start;
    private boolean numFoundExact;
    private ArrayList<Object> docs;

    public int getNumFound() {
        return numFound;
    }

    public void setNumFound(int numFound) {
        this.numFound = numFound;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public boolean isNumFoundExact() {
        return numFoundExact;
    }

    public void setNumFoundExact(boolean numFoundExact) {
        this.numFoundExact = numFoundExact;
    }

    public ArrayList<Object> getDocs() {
        return docs;
    }

    public void setDocs(ArrayList<Object> docs) {
        this.docs = docs;
    }
}
