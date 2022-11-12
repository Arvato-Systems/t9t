/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.jetty.init;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * See: http://jira.esd.arvato-systems.de/browse/FT-2775
 */
@Provider
public class CustomLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLoggingFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    // constructor solely used for debugging / logging - temporarily
    public  CustomLoggingFilter() {
        LOGGER.debug("Creating new instance");
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            //execution time
            MDC.put("start-time", String.valueOf(System.nanoTime() / 1000L));
            LOGGER.debug("HTTP REQUEST   : {} {}{}", requestContext.getMethod(), requestContext.getUriInfo().getPath(),
              toStringQueryParams(requestContext.getUriInfo().getQueryParameters()));
            if (shouldLog(requestContext.getMethod(), requestContext.getUriInfo().getPath())) {
                LOGGER.debug("   Call        : {}#{} ", resourceInfo.getResourceClass().getSimpleName(), resourceInfo.getResourceMethod().getName());
                if (!requestContext.getUriInfo().getPathParameters().isEmpty() && LOGGER.isDebugEnabled()) {
                    LOGGER.debug("   Path Parms  : {}", toString(requestContext.getUriInfo().getPathParameters()));
                }
                LOGGER.debug("   Header      : {}", requestContext.getHeaders());
                final String entityStream = readEntityStream(requestContext);
                LOGGER.debug("   Content Type: {}", requestContext.getMediaType());
                LOGGER.debug("   Body        : {}", (entityStream.isEmpty() ? "#EMPTY#" : entityStream));
            }
        }
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("HTTP RESPONSE  : {} {} ", requestContext.getMethod(), requestContext.getUriInfo().getPath());
            LOGGER.debug("   Return Code : {} {} {}", responseContext.getStatus(), responseContext.getStatusInfo().getFamily().name(),
              responseContext.getStatusInfo().getReasonPhrase());
            if (shouldLog(requestContext.getMethod(), requestContext.getUriInfo().getPath())) {
                LOGGER.debug("   Header      : {}", responseContext.getHeaders());
                LOGGER.debug("   Content Type: {}", responseContext.getMediaType());
                LOGGER.debug("   Content     : {}", responseContext.getEntity());

                final String stTime = MDC.get("start-time");
                if (stTime != null && stTime.length() > 0) {
                    final long startTime = Long.parseLong(stTime);
                    final long executionTime = System.nanoTime() / 1000L - startTime;
                    LOGGER.debug("Total request execution time: {} microseconds", executionTime);
                }
            }
            //clear the context on exit
            MDC.clear();
        }
    }

    private boolean shouldLog(final String method, final String path) {
        if (!"GET".equals(method)) {
            return true;  // only omit certain GET requests
        }
        if ("/openapi.json".equals(path)) {
            return false;
        }
        if (path != null && path.startsWith("/swagger-ui/")) {
            return false;
        }
        return true;
    }

    private String readEntityStream(final ContainerRequestContext requestContext) {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final InputStream inputStream = requestContext.getEntityStream();
        final StringBuilder builder = new StringBuilder();
        try {
            writeTo(inputStream, outStream);
            final byte[] requestEntity = outStream.toByteArray();
            if (requestEntity.length > 0) {
                builder.append(new String(requestEntity));
            }
            requestContext.setEntityStream(new ByteArrayInputStream(requestEntity));
        } catch (final IOException ex) {
            LOGGER.debug("    ----Exception occurred while reading entity stream :{}", ex.getMessage());
        }
        return builder.toString();
    }

    protected static void writeTo(final InputStream in, final OutputStream out) throws IOException {
        int read;
        final byte[] data = new byte[8192];
        while ((read = in.read(data)) != -1) {
            out.write(data, 0, read);
        }
    }

    private static String toString(final MultivaluedMap<String, String> map) {
        final Iterator<Entry<String, List<String>>> i = map.entrySet().iterator();
        if (!i.hasNext()) return "{}";

        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (;;) {
            final Entry<String, List<String>> entry = i.next();
            builder.append(entry.getKey()).append("=").append(entry.getValue());
          if (!i.hasNext())
              return builder.append('}').toString();
          builder.append(',').append(' ');
        }
    }

    private static String toStringQueryParams(final MultivaluedMap<String, String> map) {
        final Iterator<Entry<String, List<String>>> i = map.entrySet().iterator();
        if (!i.hasNext()) return "";

        final StringBuilder builder = new StringBuilder("?");
        for (;;) {
            final Entry<String, List<String>> entry = i.next();
            String value = "";
            final List<String> v = entry.getValue();
            if (!v.isEmpty())
                value = v.get(0);
            builder.append(entry.getKey()).append("=").append(value);
            if (!i.hasNext())
                return builder.toString();
            builder.append('&');
        }
    }
}
