/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.ai.adobe.service.impl;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.adobe.T9tAdobeConstants;
import com.arvatosystems.t9t.ai.adobe.T9tAdobeException;
import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageRequest;
import com.arvatosystems.t9t.ai.adobe.request.AdobeFireflyGenerateImageResponse;
import com.arvatosystems.t9t.ai.adobe.service.IAdobeFireflyClient;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

@Singleton
public class AdobeFireflyClient implements IAdobeFireflyClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdobeFireflyClient.class);
    private static final String IMAGES_GENERATE_PATH = "/v3/images/generate-async";
    private static final String IMAGES_STATUS_PATH = "/v3/status"; // + "/" + jobId
    private static final int DEFAULT_POLLING_INTERVAL_MS = 500; // 0.5 seconds
    private static final int DEFAULT_TIMEOUT_MS = 10000; // 10 seconds

    private final boolean configured;
    private final ObjectMapper objectMapper;
    private final Duration timeoutInMilliseconds;
    private final String authentication;
    private final String clientId;
    private final String url;
    private final HttpClient httpClient;
    private final int pollingIntervalMs;
    private final int timeoutMs;
    private final int maxPollingAttempts;

    public AdobeFireflyClient() {
        final UplinkConfiguration config = ConfigProvider.getUplink(T9tAdobeConstants.UPLINK_KEY_ADOBE);
        LOGGER.info("Adobe config is {}", config);
        configured = config != null && config.getUrl() != null && config.getClientId() != null;
        if (!configured) {
            LOGGER.info("No or incomplete configuration for Adobe Firefly, service is disabled.");
            objectMapper = null;
            timeoutInMilliseconds = null;
            url = null;
            authentication = null;
            clientId = null;
            httpClient = null;
            pollingIntervalMs = DEFAULT_POLLING_INTERVAL_MS;
            timeoutMs = DEFAULT_TIMEOUT_MS;
            maxPollingAttempts = 0;
        } else {
            LOGGER.info("Setting up Adobe Firefly client.");
            objectMapper = JacksonTools.createObjectMapper();
            timeoutInMilliseconds = Duration.ofMillis(T9tUtil.nvl(config.getTimeoutInMs(), 30000));
            url = config.getUrl();
            authentication = config.getBasicAuth() == null ? null : T9tAdobeConstants.ADOBE_HTTP_AUTH_PREFIX + config.getBasicAuth();
            clientId = config.getClientId();

            // Allow configurable polling interval and timeout (milliseconds)
            pollingIntervalMs = DEFAULT_POLLING_INTERVAL_MS;
            timeoutMs = T9tUtil.nvl(config.getRequestTimeoutInMs(), DEFAULT_TIMEOUT_MS);
            maxPollingAttempts = timeoutMs / pollingIntervalMs + 1;

            httpClient = HttpClient.newBuilder()
                    .version(Version.HTTP_2)
                    .connectTimeout(timeoutInMilliseconds)
                    .build();
            LOGGER.info("Adobe Firefly client initialized with polling interval {}ms and timeout {}ms.", pollingIntervalMs, timeoutMs);
        }
    }

    @Nonnull
    @Override
    public AdobeFireflyGenerateImageResponse generateImage(@Nonnull final RequestContext ctx,
            @Nonnull final AdobeFireflyGenerateImageRequest request) throws Exception {
        if (!configured) {
            throw new T9tException(T9tAdobeException.ADOBE_NOT_CONFIGURED);
        }

        final String headerModelVersion;
        // Build the request JSON
        final ObjectNode requestJson = objectMapper.createObjectNode();
        if (request.getCustomModelId() != null) {
            requestJson.put("customModelId", request.getCustomModelId());
            headerModelVersion = T9tUtil.nvl(request.getModelVersion(), T9tAdobeConstants.ADOBE_DEFAULT_CUSTOM_MODEL_VERSION);
        } else {
            headerModelVersion = T9tUtil.nvl(request.getModelVersion(), T9tAdobeConstants.ADOBE_DEFAULT_MODEL_VERSION);
        }
        requestJson.put("numVariations", 1);
        requestJson.put("prompt", request.getPositivePrompt());

        if (request.getNegativePrompt() != null) {
            requestJson.put("negativePrompt", request.getNegativePrompt());
        }

        if (request.getSeed() != null) {
            requestJson.put("seed", request.getSeed());
        }

        // Add size if specified
        final ObjectNode sizeNode = objectMapper.createObjectNode();
        sizeNode.put("width",  T9tUtil.nvl(request.getWidth(),  T9tAdobeConstants.ADOBE_DEFAULT_WIDTH));
        sizeNode.put("height", T9tUtil.nvl(request.getHeight(), T9tAdobeConstants.ADOBE_DEFAULT_HEIGHT));
        requestJson.set("size", sizeNode);

        // Add contentClass for model version
        // requestJson.put("contentClass", "art" or "photo");

        final String requestBody = objectMapper.writeValueAsString(requestJson);
        LOGGER.debug("Adobe Firefly request: {}", requestBody);

        // Submit the async job
        final HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder(new URI(url + IMAGES_GENERATE_PATH))
                .version(Version.HTTP_2)
                .POST(BodyPublishers.ofString(requestBody))
                .timeout(timeoutInMilliseconds)
                .header(T9tAdobeConstants.ADOBE_HEADER_X_API_KEY, clientId)
                .header(T9tConstants.HTTP_HEADER_CONTENT_TYPE, "application/json");

        if (authentication != null) {
            httpRequestBuilder.header(T9tConstants.HTTP_HEADER_AUTH, authentication);
        }
        final HttpRequest httpRequest = httpRequestBuilder.build();

        final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        final int statusCode = response.statusCode();

        if (statusCode == 202) {
            // Async job submitted successfully, get the job ID from Location header or response body
            final String responseBody = response.body();
            LOGGER.debug("Adobe Firefly async job submitted, response: {}", responseBody);

            // Poll for results
            return pollForResult(responseBody);
        } else {
            LOGGER.error("Adobe Firefly HTTP error: status={}, body={}", statusCode, response.body());
            throw new T9tException(T9tAdobeException.ADOBE_HTTP_ERROR, String.valueOf(statusCode));
        }
    }

    private AdobeFireflyGenerateImageResponse pollForResult(final String initialResponse) throws Exception {
        JsonNode responseNode = objectMapper.readTree(initialResponse);

        // Check if we have outputs immediately (synchronous response)
        if (responseNode.has("outputs") && responseNode.get("outputs").isArray() && responseNode.get("outputs").size() > 0) {
            LOGGER.debug("Received immediate output of {} items", responseNode.get("outputs").size());
            return extractResult(responseNode);
        }

        // Async polling: check for job status URL or ID
        String jobId = null;
        if (responseNode.has("jobId")) {
            jobId = responseNode.get("jobId").asText();
        }

        if (jobId == null) {
            throw new T9tException(T9tAdobeException.ADOBE_INVALID_RESPONSE, "No jobId in response");
        }

        // Poll for completion
        for (int attempt = 0; attempt < maxPollingAttempts; attempt++) {
            LOGGER.debug("Polling attempt {}/{} for jobId {}", attempt + 1, maxPollingAttempts, jobId);
            Thread.sleep(pollingIntervalMs);

            // Check job status
            final String statusUrl = url + IMAGES_STATUS_PATH + "/" + jobId;
            final HttpRequest.Builder statusRequestBuilder = HttpRequest.newBuilder(new URI(statusUrl))
                    .version(Version.HTTP_2)
                    .GET()
                    .timeout(timeoutInMilliseconds)
                    .header(T9tAdobeConstants.ADOBE_HEADER_X_API_KEY, clientId);

            if (authentication != null) {
                statusRequestBuilder.header(T9tConstants.HTTP_HEADER_AUTH, authentication);
            }
            final HttpRequest statusRequest = statusRequestBuilder.build();

            final HttpResponse<String> statusResponse = httpClient.send(statusRequest, HttpResponse.BodyHandlers.ofString());

            if (statusResponse.statusCode() != 200) {
                LOGGER.error("Adobe Firefly status check failed: status={}, body={}", statusResponse.statusCode(), statusResponse.body());
                throw new T9tException(T9tAdobeException.ADOBE_HTTP_ERROR, String.valueOf(statusResponse.statusCode()));
            }

            responseNode = objectMapper.readTree(statusResponse.body());
            final String status = responseNode.has("status") ? responseNode.get("status").asText() : null;

            if (isJobCompleted(responseNode, status)) {
                // Job completed successfully
                final JsonNode resultNode = responseNode.get("result");
                LOGGER.debug("I think the job is completed with status {}, result is {}", status, resultNode);
                return extractResult(resultNode);
            } else if ("failed".equals(status)) {
                // Job failed
                final String errorMessage = responseNode.has("error") ? responseNode.get("error").asText() : "Unknown error";
                LOGGER.error("Adobe Firefly job failed: {}", errorMessage);
                throw new T9tException(T9tAdobeException.ADOBE_JOB_FAILED, errorMessage);
            }
            // Otherwise, continue polling (status is probably "running" or "pending")
        }

        // Timeout
        LOGGER.error("Adobe Firefly job timed out after {} ms", timeoutMs);
        throw new T9tException(T9tAdobeException.ADOBE_TIMEOUT);
    }

    private boolean isJobCompleted(final JsonNode responseNode, final String status) {
        return "succeeded".equals(status)
                || (responseNode.has("outputs") && responseNode.get("outputs").isArray() && responseNode.get("outputs").size() > 0);
    }

    private AdobeFireflyGenerateImageResponse extractResult(final JsonNode responseNode) throws Exception {
        final AdobeFireflyGenerateImageResponse response = new AdobeFireflyGenerateImageResponse();

        // Extract outputs
        if (responseNode.has("outputs") && responseNode.get("outputs").isArray() && responseNode.get("outputs").size() > 0) {
            final JsonNode output = responseNode.get("outputs").get(0);
            if (output.has("image") && output.get("image").has("url")) {
                response.setImageUrl(output.get("image").get("url").asText());
            }
            if (output.has("seed")) {
                response.setSeed(output.get("seed").asInt());
            }
        }

        if (response.getImageUrl() == null) {
            throw new T9tException(T9tAdobeException.ADOBE_INVALID_RESPONSE, "No image URL in response");
        }

        return response;
    }
}
