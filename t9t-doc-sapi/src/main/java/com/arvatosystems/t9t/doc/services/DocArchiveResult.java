package com.arvatosystems.t9t.doc.services;

public class DocArchiveResult {
    public final Long sinkRef;
    public final String fileOrQueueName;

    public DocArchiveResult(final Long sinkRef, final String fileOrQueueName) {
        this.sinkRef = sinkRef;
        this.fileOrQueueName = fileOrQueueName;
    }
}
