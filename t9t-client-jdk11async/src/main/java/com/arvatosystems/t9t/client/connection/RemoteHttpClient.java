package com.arvatosystems.t9t.client.connection;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.core.MimeTypes;

class RemoteHttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteHttpClient.class);

    protected final AtomicInteger threadCounter = new AtomicInteger();
    protected final AtomicInteger nextConnection = new AtomicInteger(0);
    protected final int poolSize;
    protected final List<SingleConnection> httpClients;
    private final ExecutorService executorService;

    protected void statisticsOut() {
        for (final SingleConnection me: httpClients) {
            LOGGER.info("Connection {}: current = {}, peak = {}, total = {}", me.index, me.currentPending.get(), me.peakUse.get(), me.totalUses.get());
        }
    }

    public RemoteHttpClient(int threadPoolSize, int parallelConnections) {
        LOGGER.info("Creating new HttpClient for remote connections, using {} threads and {} separate instances", threadPoolSize, parallelConnections);
        executorService = Executors.newFixedThreadPool(threadPoolSize, (r) -> {
            final String threadName = "t9t-http-async-" + threadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for asynchronous http response processing", threadName, threadPoolSize);
            return new Thread(r, threadName);
        });

        httpClients = new ArrayList<>(parallelConnections);
        poolSize = parallelConnections;
        for (int i = 0; i < parallelConnections; ++i) {
            httpClients.add(new SingleConnection(i, executorService));
        }
    }

    protected SingleConnection pickConnectionToUse() {
        // first choice is the next one, round robin,
        int initialChoiceIndex = nextConnection.incrementAndGet();
        if (initialChoiceIndex > 1_000_000) {
            nextConnection.set(0);
            executorService.submit(() -> statisticsOut());
        }
        initialChoiceIndex = initialChoiceIndex % poolSize;
        final SingleConnection initialChoice = httpClients.get(initialChoiceIndex);
        // use this, unless we find a connection which is a lot less active
        final int pendingForinitialChoice = initialChoice.currentPending.get();
        if (pendingForinitialChoice < 20) {
            return initialChoice;
        }
        final int limit = pendingForinitialChoice / 2;  // at least 10...
        // fall back for selection of initial best
        for (int i = 0; i < poolSize; ++i) {
            final SingleConnection alternateChoice = httpClients.get(i);
            if (alternateChoice.currentPending.get() <= limit) {
                return alternateChoice;
            }
        }
        return initialChoice;
    }

    public HttpRequest buildRequest(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final CompactByteArrayComposer bac = new CompactByteArrayComposer(false);
        bac.writeRecord(request);
        bac.close();

        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(uri)
                .version(Version.HTTP_2)
                .POST(BodyPublishers.ofByteArray(bac.getBuffer(), 0, bac.getLength()))
                .timeout(Duration.ofSeconds(55));

        if (authentication != null)
            httpRequestBuilder.header("Authorization", authentication);

        httpRequestBuilder.header("Content-Type",   MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header("Accept",         MimeTypes.MIME_TYPE_COMPACT_BONAPARTE);
        httpRequestBuilder.header("Charset",        "utf-8");
        httpRequestBuilder.header("Accept-Charset", "utf-8");
        return httpRequestBuilder.build();
    }

    private HttpPostResponseObject response2object(final HttpResponse<byte[]> response) {
        final int returnCode = response.statusCode();
        LOGGER.debug("*** HTTP Response {}, connection type {}", returnCode, response.version());
        if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {   // accept 200, 201, etc...
            LOGGER.warn("response is HTTP {} ({})", returnCode, null);
            return new HttpPostResponseObject(returnCode, null, null);
        }
        final byte[] receivedBuffer = response.body();
        final BonaPortable obj = new CompactByteArrayParser(receivedBuffer, 0, -1).readRecord();
        return new HttpPostResponseObject(returnCode, String.valueOf(returnCode), obj);
    }

    public CompletableFuture<HttpPostResponseObject> doIO(final URI uri, final String authentication, final BonaPortable request) throws Exception {
        final HttpRequest httpRq = buildRequest(uri, authentication, request);
        final BodyHandler<byte[]> serializedRequest = HttpResponse.BodyHandlers.ofByteArray();
        final SingleConnection myConnection = pickConnectionToUse();
        final int newHigh = myConnection.currentPending.incrementAndGet();
        if (newHigh > myConnection.peakUse.get()) {
            myConnection.peakUse.set(newHigh);
        }
        final CompletableFuture<HttpResponse<byte[]>> responseF = myConnection.httpClient.sendAsync(httpRq, serializedRequest);
        return responseF.thenApply(response -> {
            myConnection.currentPending.decrementAndGet();
            myConnection.totalUses.incrementAndGet();
            return response2object(response);
        });
    }
}
