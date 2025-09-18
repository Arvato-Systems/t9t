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
package com.arvatosystems.t9t.rest.delegates;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * Delegate to make it possible to add additional header parameters.
 */
public class HttpHeadersDelegate implements HttpHeaders {

    private final HttpHeaders httpHeaders;
    private final MultivaluedMap<String, String> mergedHeaders;

    public HttpHeadersDelegate(final HttpHeaders httpHeaders, final MultivaluedMap<String, String> overriddenHeaders) {
        this.httpHeaders = httpHeaders;

        mergedHeaders = new MultivaluedHashMap<>(httpHeaders.getRequestHeaders().size() + overriddenHeaders.size());
        mergedHeaders.putAll(httpHeaders.getRequestHeaders());
        mergedHeaders.putAll(overriddenHeaders);
    }

    @Override
    public List<String> getRequestHeader(final String name) {
        return mergedHeaders.get(name);
    }

    @Override
    public String getHeaderString(final String name) {
        final List<String> vals = mergedHeaders.get(name);
        if (vals == null) {
            return null;
        }
        return String.join(",", vals);
    }

    @Override
    public MultivaluedMap<String, String> getRequestHeaders() {
        return mergedHeaders;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return httpHeaders.getAcceptableMediaTypes();
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        return httpHeaders.getAcceptableLanguages();
    }

    @Override
    public MediaType getMediaType() {
        return httpHeaders.getMediaType();
    }

    @Override
    public Locale getLanguage() {
        return httpHeaders.getLanguage();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        return httpHeaders.getCookies();
    }

    @Override
    public Date getDate() {
        return httpHeaders.getDate();
    }

    @Override
    public int getLength() {
        return httpHeaders.getLength();
    }

    // new method with JAKARTA-WS-RS 4.0.0
//    @Override
//    public boolean containsHeaderString(final String name, final String valueSeparatorRegex, final Predicate<String> valuePredicate) {
//        return httpHeaders.containsHeaderString(name, valueSeparatorRegex, valuePredicate);
//    }
}
