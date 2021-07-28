package com.arvatosystems.t9t.client.connection;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleConnection {
    protected final int index;
    protected final HttpClient httpClient;
    protected final AtomicInteger currentPending = new AtomicInteger(0);
    protected final AtomicInteger totalUses = new AtomicInteger(0);
    protected final AtomicInteger peakUse = new AtomicInteger(0);

    protected SingleConnection(final int instanceNo, final ExecutorService executorService) {
        index = instanceNo;
        httpClient = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .executor(executorService)
                .build();
    }
}
