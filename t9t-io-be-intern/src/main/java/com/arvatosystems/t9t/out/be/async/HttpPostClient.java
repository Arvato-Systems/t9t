package com.arvatosystems.t9t.out.be.async;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.HttpPostResponseObject;
import de.jpaw.bonaparte.util.IMarshaller;
import de.jpaw.bonaparte.util.impl.RecordMarshallerBonaparte;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;

/**
 * Client connection via http. This class provides a request / response functionality implemented via http POST.
 * The http authorization header can be modified during the lifetime of an instance.
 * The marshaller can be changed as well, this is however usually not desired.
 */
public class HttpPostClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpPostClient.class);

    protected String baseUrl;
    private final boolean addVariableUrlPath;
    private final boolean logSizes;
    private final boolean logText;
    private final boolean logHex;
    private URI cachedUri = null;
    protected IMarshaller marshaller;
    protected String authentication = null;
    protected int timeoutInMs = 5000;
    private volatile HttpClient httpClient;

    public HttpPostClient(String baseUrl, boolean addVariableUrlPath, boolean logSizes, boolean logText, boolean logHex, IMarshaller initialMarshaller) {
        this.baseUrl            = baseUrl;
        this.addVariableUrlPath = addVariableUrlPath;
        this.logSizes           = logSizes;
        this.logText            = logText;
        this.logHex             = logHex;
        this.marshaller         = initialMarshaller;
    }

    public HttpPostClient(String baseUrl) {
        this(baseUrl, false, false, false, false, new RecordMarshallerBonaparte());
    }

    public void setMarshaller(IMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void setTimeoutInMs(int newTimeout) {
        this.timeoutInMs = newTimeout;
        this.httpClient = null;
    }

    public void setAuthentication(final String authentication) {
        this.authentication = authentication;
    }

    public void setBaseUrl(final String baseUrl) {
        this.baseUrl = baseUrl;
        cachedUri = null;       // reset cached converted URL if changing the base path!
    }

    /** Configure request headers before the request is sent. */
    protected void setRequestProperties(@Nonnull final HttpRequest.Builder request) {
        request.header("Content-Type",   marshaller.getContentType());
        request.header("Accept",         marshaller.getContentType());
        request.header("Charset",        "utf-8");
        request.header("Accept-Charset", "utf-8");
        if (authentication != null) {
            request.header("Authorization", authentication);
        }
    }

    /** Creates the reusable JDK HTTP client. Subclasses may override to customize client behavior. */
    protected HttpClient createHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeoutInMs))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    private HttpClient getHttpClient() {
        HttpClient current = httpClient;
        if (current == null) {
            synchronized (this) {
                current = httpClient;
                if (current == null) {
                    current = createHttpClient();
                    httpClient = current;
                }
            }
        }
        return current;
    }

    protected void requestLogger(String pqon, ByteArray serializedRequest) {
        if (logSizes) {
            LOGGER.info("{} serialized as {} bytes for MIME type {}", pqon, serializedRequest.length(), marshaller.getContentType());
        }
        if (logText) {
            LOGGER.info("Request is <{}>", serializedRequest.toString());
        }
        if (logHex) {
            LOGGER.info(serializedRequest.hexdump(0, 0));
        }
    }

    protected void responseLogger(ByteBuilder serializedResponse) {
        if (logSizes) {
            LOGGER.info("retrieved {} bytes response", serializedResponse.length());
        }
        if (logText) {
            LOGGER.info("Response is <{}>", serializedResponse.toString());
        }
        if (logHex) {
            LOGGER.info(serializedResponse.hexdump(0, 0));
        }
    }

    protected BonaPortable errorReturn(String requestPqon, int returnCode, String statusMessage) throws Exception {
        LOGGER.warn("response for {} is HTTP {} ({})", requestPqon, returnCode, statusMessage);
        return null;
    }

    /** Execute the request / response dialog. */
    public BonaPortable doIO(BonaPortable request) throws Exception {
        return doIO2(request).getResponseObject();
    }

    /** Execute the request / response dialog. */
    public HttpPostResponseObject doIO2(BonaPortable request) throws Exception {
        ByteArray serializedRequest = ByteArray.ZERO_BYTE_ARRAY;
        String requestPqon          = "LOGOUT";

        if (request != null) {
            serializedRequest   = marshaller.marshal(request);
            requestPqon         = request.ret$PQON();
        }

        URI uri = null;
        if (addVariableUrlPath && request != null) {
            String variablePath = request.ret$BonaPortableClass().getProperty("path");
            if (variablePath != null) {
                uri = URI.create(baseUrl + "/" + variablePath);
            }
        }
        if (uri == null) {
            if (cachedUri == null) {
                cachedUri = URI.create(baseUrl);
            }
            uri = cachedUri;
        }

        final ByteArrayOutputStream requestBytes = new ByteArrayOutputStream(serializedRequest.length());
        serializedRequest.toOutputStream(requestBytes);
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBytes.toByteArray()));
        setRequestProperties(requestBuilder);
        requestLogger(requestPqon, serializedRequest);

        final CompletableFuture<HttpResponse<byte[]>> responseFuture = getHttpClient().sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        final HttpResponse<byte[]> response;
        try {
            response = responseFuture.get(timeoutInMs, TimeUnit.MILLISECONDS);
        } finally {
            if (!responseFuture.isDone()) {
                responseFuture.cancel(true);
            }
        }
        int returnCode = response.statusCode();
        // JDK HttpClient intentionally does not expose the HTTP reason phrase.
        final String statusMessage = null;

        if ((returnCode / 100) != (HttpURLConnection.HTTP_OK / 100)) {   // accept 200, 201, etc...
            BonaPortable errorResp = errorReturn(requestPqon, returnCode, statusMessage);
            return new HttpPostResponseObject(returnCode, statusMessage, errorResp);
        }

        // The response body is already buffered; no network reads occur here.
        final ByteBuilder serializedResponse = new ByteBuilder();
        try (InputStream is = new ByteArrayInputStream(response.body())) {
            serializedResponse.readFromInputStream(is, 0);
        }

        responseLogger(serializedResponse);

        return new HttpPostResponseObject(returnCode, statusMessage, serializedResponse.length() == 0 ? null : marshaller.unmarshal(serializedResponse));
    }
}
