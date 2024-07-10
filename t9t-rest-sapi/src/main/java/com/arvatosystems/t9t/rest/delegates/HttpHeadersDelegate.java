package com.arvatosystems.t9t.rest.delegates;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Delegate to make it possible to add additional header parameters.
 */
class HttpHeadersDelegate implements HttpHeaders {

    private final HttpHeaders httpHeaders;
    private final MultivaluedMap<String, String> mergedHeaders;

    HttpHeadersDelegate(final HttpHeaders httpHeaders, final MultivaluedMap<String, String> overriddenHeaders) {
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

}
