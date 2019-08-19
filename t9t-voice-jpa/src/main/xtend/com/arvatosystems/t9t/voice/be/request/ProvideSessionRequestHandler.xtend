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
package com.arvatosystems.t9t.voice.be.request

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.search.EnumFilter
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.voice.VoiceProvider
import com.arvatosystems.t9t.voice.jpa.mapping.IVoiceApplicationDTOMapper
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver
import com.arvatosystems.t9t.voice.request.ProvideSessionRequest
import com.arvatosystems.t9t.voice.request.ProvideSessionResponse
import com.arvatosystems.t9t.voice.request.VoiceApplicationSearchRequest
import com.arvatosystems.t9t.voice.services.IVoicePersistenceAccess
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.SearchFilters
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import de.jpaw.dp.Inject

@AddLogger
class ProvideSessionRequestHandler extends AbstractRequestHandler<ProvideSessionRequest> {
    @Inject protected IExecutor               executor
    @Inject protected IVoicePersistenceAccess persistenceAccess
    @Inject protected IVoiceApplicationEntityResolver applicationResolver
    @Inject protected IVoiceApplicationDTOMapper      applicationMapper


    override execute(RequestContext ctx, ProvideSessionRequest rq) {
        // retrieve the application
        val appSearchRequest = new VoiceApplicationSearchRequest => [
            searchFilter = SearchFilters.and(
                new UnicodeFilter("providerId") => [
                    equalsValue = rq.applicationId
                ],
                new EnumFilter("provider", VoiceProvider.ALEXA.ret$PQON) => [
                    equalsName = rq.provider.name
                ]
            )
        ]
        val apps = applicationResolver.search(appSearchRequest)
        if (apps.size != 1) {
            LOGGER.error("No voice application of ID {} found for provider {}", rq.applicationId, rq.provider)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST)
        }
        val applicationDTO = applicationMapper.mapToDto(apps.get(0))
        if (!applicationDTO.isActive) {
            throw new T9tException(T9tException.RECORD_INACTIVE)
        }
        val resp = new ProvideSessionResponse => [
            tenantRef      = apps.get(0).tenantRef
            application    = applicationDTO
        ]
        if (rq.userId !== null) {
            // get internal user for userId
            resp.user = persistenceAccess.getUserForExternalId(resp.tenantRef, applicationDTO.objectRef, rq.userId)
        }
        return resp
    }
}
