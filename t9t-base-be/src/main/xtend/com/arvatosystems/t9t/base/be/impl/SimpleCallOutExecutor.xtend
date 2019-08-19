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
package com.arvatosystems.t9t.base.be.impl

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.IForeignRequest
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.sock.HttpPostClient
import de.jpaw.bonaparte.util.IMarshaller
import de.jpaw.bonaparte.util.impl.RecordMarshallerBonaparte
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity
import de.jpaw.dp.Singleton

/** Simple (unoptimized) call out - replace by pooling connection handler, for example Apache HttpClient. */

@AddLogger
@Singleton
class SimpleCallOutExecutor implements IForeignRequest {
    protected final String url
    protected final MediaType mediaType
    private static final String FALLBACK_URL = "http://localhost:9999"

    new() {
        LOGGER.info("Simple call out connector - standard")
        val cfg = ConfigProvider.configuration.uplinkConfiguration
        val baseUrl = cfg?.url
        url = if (baseUrl === null) {
            LOGGER.error("Improper configuration - no uplink URL defined, using {}", FALLBACK_URL)
            FALLBACK_URL
        } else {
            LOGGER.info("Uplink configured to call out to {}", baseUrl)
            baseUrl
        }
        mediaType = cfg?.mediaType ?: MediaType.COMPACT_BONAPARTE
    }

    new(String url, MediaType mediaType) {
        LOGGER.info("Simple call out connector - custom for URL {}, mediaType {}", url, mediaType)
        this.url = url
        this.mediaType = mediaType
    }

    override execute(RequestContext ctx, RequestParameters rp) {
        var IMarshaller marshaller = null
        switch (mediaType) {
            case COMPACT_BONAPARTE:
                marshaller = new RecordMarshallerCompactBonaparteIdentity
            case BONAPARTE:
                marshaller = new RecordMarshallerBonaparte
            default: {
                LOGGER.error("Improper configuration - uplink does not supper media type {}", mediaType)
                return new ServiceResponse => [
                    returnCode = T9tException.UPSTREAM_BAD_MEDIA_TYPE
                    errorDetails = mediaType.name
                ]
            }
        }
        val dlg = new HttpPostClient(url, false, true, false, false, marshaller)
        dlg.authentication = "Bearer " + ctx.internalHeaderParameters.encodedJwt
        val resp = dlg.doIO(rp)
        if (resp === null)
            return new ServiceResponse => [
                returnCode = T9tException.UPSTREAM_NULL_RESPONSE
            ]
        if (resp instanceof ServiceResponse) {
            return resp
        } else {
            return new ServiceResponse => [
                returnCode   = T9tException.UPSTREAM_BAD_RESPONSE
                errorDetails = resp.ret$PQON
            ]
        }
    }
}
