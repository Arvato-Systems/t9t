/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.remote.connect

import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.util.IMarshaller
import de.jpaw.bonaparte.util.impl.RecordMarshallerJson
import de.jpaw.util.ByteBuilder
import java.net.HttpURLConnection
import java.net.URL
import de.jpaw.util.ByteArray
import java.nio.charset.StandardCharsets
import java.io.InputStream

@AddLogger
class RESTConnection extends ConnectionDefaults {
    protected IMarshaller marshaller = new RecordMarshallerJson();
    protected String authentication = null;
    protected final String baseRestUrl;

    new() {
        baseRestUrl = getInitialRestUrl
    }

    new(String baseRestUrl) {
        this.baseRestUrl = baseRestUrl
    }

    def void setMarshaller(IMarshaller marshaller) {
        this.marshaller = marshaller
    }

    def void setAuthentication(String authentication) {
        this.authentication = authentication
    }

    def protected void setRequestProperties(HttpURLConnection connection) {
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(5000);
        connection.setRequestProperty("Content-Type",   marshaller.getContentType());
        connection.setRequestProperty("Accept",         marshaller.getContentType());
        connection.setRequestProperty("Charset",        "utf-8");
        connection.setRequestProperty("Accept-Charset", "utf-8");
        connection.setUseCaches(false);
    }

    def boolean isOK(int httpCode) {
        return httpCode >= 200 && httpCode <= 299
    }
    def boolean sendsPayload(int httpCode) {
        return isOK(httpCode) || (httpCode >= 400 && httpCode <= 500)
    }

    def ByteBuilder doIO(URL url, ByteArray request, boolean expectError) {
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"

        setRequestProperties(connection);
        if (authentication !== null)
            connection.setRequestProperty("Authorization", authentication);

        connection.setRequestProperty("Content-Length", "" + request.length());

        // write the request
        val wr = connection.getOutputStream();
        request.toOutputStream(wr);
        wr.flush();

        // the status should be available after getInputStream for GET, but before for POST
        // in either case, swapping the order would cause a nasty IOException!

        val returnCode = connection.getResponseCode();
        val statusMessage = connection.getResponseMessage();

        if (expectError) {
            if (isOK(returnCode))
                LOGGER.error("REST request successful, but expected error");
        } else {
            if (!isOK(returnCode))
                LOGGER.error("REST request failed: " + returnCode + ": " + statusMessage);
        }

        // retrieve the response or error response
        var InputStream is = null
        try {
            is = connection.getInputStream();
        } catch (Exception e) {
            is = connection.getErrorStream();
        }

        val serializedResponse = new ByteBuilder();
        serializedResponse.readFromInputStream(is, 0);
        is.close();
        wr.close();
//        if (!isOK(returnCode)) {
//            LOGGER.error("Received return code {}: {}", returnCode, statusMessage)
//            LOGGER.error("Response was {}", serializedResponse)
//            throw new RuntimeException("Server denied")
//        }
        return serializedResponse
    }

    def BonaPortable doIO(BonaPortable request) throws Exception {
        val serializedRequest = marshaller.marshal(request)

        val variablePath = request.ret$BonaPortableClass().getProperty("path");
        LOGGER.debug("Sending object {} to URL {}/{} in format {}", request.ret$PQON, baseRestUrl, variablePath, marshaller.getContentType())
        val url = new URL(baseRestUrl + "/" + variablePath);

        val serializedResponse = doIO(url, serializedRequest, false)

        return if (serializedResponse !== null && serializedResponse.length() > 0) marshaller.unmarshal(serializedResponse);
    }

    def String doIO(String request, String path, boolean expectError) throws Exception {
        LOGGER.debug("Sending String to URL {}/{} in format {}", baseRestUrl, path, marshaller.getContentType())
        val url = new URL(baseRestUrl + "/" + path);

        val response = doIO(url, ByteArray.fromString(request, StandardCharsets.UTF_8), expectError)
        if (response !== null && response.length() > 0) {
            response.charset = StandardCharsets.UTF_8
            return response.toString;
        }
        return null
    }
}
