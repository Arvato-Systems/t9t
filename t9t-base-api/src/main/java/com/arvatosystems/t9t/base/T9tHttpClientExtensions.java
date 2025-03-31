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
package com.arvatosystems.t9t.base;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MapComposer;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import jakarta.annotation.Nonnull;

/**
 * Some extensions for the JDK 11 HTTP client for multipart.
 */
public final class T9tHttpClientExtensions {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tHttpClientExtensions.class);
    private static final String CRLF = "\r\n";
    private static final String QUOTE = "\"";
    private static final String DASHES = "--";

    private T9tHttpClientExtensions() { }

    /**
     * Converts a map of key-value pairs into a URL encoded string.
     *
     * @param data The map of key/value pairs
     *
     * @return the URL encoded string
     */
    public static String urlEncode(@Nonnull final Map<String, Object> data) {
        final StringBuilder sb = new StringBuilder(200);
        boolean notFirst = false;
        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                if (notFirst) {
                    sb.append('&');
                } else {
                    notFirst = true;
                }
                sb.append(entry.getKey());
                sb.append('=');
                sb.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }
        }
        return sb.toString();
    }

    /**
     * Provides a BodyPublisher for the JDK 11 HTTP client, for MIME content type application/x-www-form-urlencoded
     * Callers have to set the Content-Type header to "application/x-www-form-urlencoded" (<code>T9tConstants.HTTP_WWW_FORM_URLENCODED</code>).
     * This implementation prepares the data and delegates to the standard <code>HttpRequest.BodyPublishers.ofString</code>.
     *
     * @param data the data to be sent.
     */
    @Nonnull
    public static HttpRequest.BodyPublisher ofWwwFormUrlEncoded(@Nonnull final Map<String, Object> data) throws IOException {
        return BodyPublishers.ofString(urlEncode(data));
    }

    /**
     * Provides a BodyPublisher for the JDK 11 HTTP client, for MIME content type application/x-www-form-urlencoded
     * Callers have to set the Content-Type header to "application/x-www-form-urlencoded" (<code>T9tConstants.HTTP_WWW_FORM_URLENCODED</code>).
     * This implementation prepares the data and delegates to the standard <code>HttpRequest.BodyPublishers.ofString</code>.
     *
     * @param data the data to be sent.
     */
    @Nonnull
    public static HttpRequest.BodyPublisher ofWwwFormUrlEncoded(@Nonnull final BonaPortable dto) throws IOException {
        final Map<String, Object> data = MapComposer.marshal(dto, false, false);
        return BodyPublishers.ofString(urlEncode(data));
    }

    /**
     * Provides a BodyPublisher for the JDK 11 HTTP client, for MIME multipart requests of type "form-data" (RFC 2388 (7578?))
     * Callers have to set the Content-Type header to "multipart/form-data; boundary=" + <code>boundary</code>
     * where <code>boundary</code> is some unique string.
     * This implementation prepares the data and delegates to the standard <code>HttpRequest.BodyPublishers.ofByteArrays</code>.
     *
     * @param data the data to be sent. The keys are the form field names, the values are either of type <code>MediaData</code>,
     * anything else will be converted to String.
     */
    @Nonnull
    public static HttpRequest.BodyPublisher ofMultipartFormData(@Nonnull final Map<String, Object> data, @Nonnull final String boundary) throws IOException {
        if (data.isEmpty()) {
            throw new T9tException(T9tException.HTTP_MULTI_PART_NO_PARTS);
        }

        // prepare the structure for delegation
        final List<byte[]> byteArrays = new ArrayList<>(4 * data.size() + 2);

        // prepare the part separator
        final String separator = DASHES + boundary + CRLF + T9tConstants.HTTP_MULTIPART_FD_DISPOSITION;

        // emit every component
        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            final String key = entry.getKey();
            if (key.isBlank()) {
                throw new T9tException(T9tException.HTTP_MULTI_PART_EMPTY_KEY);
            }
            final Object value = entry.getValue();
            if (value == null) {
                LOGGER.error("Multipart data for key {} is not present", key);
                throw new T9tException(T9tException.HTTP_MULTI_PART_EMPTY_DATA, key);
            }
            final String separatorWithName = separator + QUOTE + key + QUOTE;

            // treat the MediaData object types separately
            if (value instanceof MediaData md) {
                final String filename = JsonUtil.getZString(md.getZ(), "filename", null);
                final String filenameInsert = filename == null ? "" : "; filename=" + QUOTE + filename + QUOTE;
                addBytes(byteArrays, separatorWithName + filenameInsert + CRLF);          // separator with optional additional filename field
                // obtain the MediaTypeDescriptor of the data
                final MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByType(md.getMediaType());
                if (mtd == null) {
                    LOGGER.error("Media type {} is not known", md.getMediaType());
                    throw new T9tException(T9tException.UNKNOWN_MEDIA_TYPE, md.getMediaType().name());
                }
                // add content type and empty line
                addBytes(byteArrays, T9tConstants.HTTP_HEADER_CONTENT_TYPE + ": " + mtd.getMimeType() + CRLF + CRLF);

                // add the data
                if (mtd.getIsText()) {
                    addBytes(byteArrays, md.getText());
                } else {
                    byteArrays.add(md.getRawData().getBytes());
                }
                addBytes(byteArrays, CRLF);
            } else {
                // anything else is converted to a string
                addBytes(byteArrays, separatorWithName + CRLF + CRLF + value.toString() + CRLF);
            }
        }

        // add final delimiter
        addBytes(byteArrays, DASHES + boundary + DASHES + CRLF);

        if (LOGGER.isTraceEnabled()) {
            traceOut(byteArrays);
        }
        // Serializing as byte array
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private static void addBytes(final List<byte[]> byteArrays, final String s) {
        byteArrays.add(s.getBytes(StandardCharsets.UTF_8));
    }

    private static void traceOut(final List<byte[]> byteArrays) {
        for (final byte[] b : byteArrays) {
            System.out.print(new String(b, StandardCharsets.UTF_8));
            // LOGGER.trace("{}", new String(b, StandardCharsets.UTF_8));  // not good, we need it without the status breaks
        }
    }
}
