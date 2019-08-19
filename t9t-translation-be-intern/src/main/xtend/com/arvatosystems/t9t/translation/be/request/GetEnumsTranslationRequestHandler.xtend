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
package com.arvatosystems.t9t.translation.be.request

import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.translation.request.EnumInstanceDTO
import com.arvatosystems.t9t.translation.request.EnumTranslationDTO
import com.arvatosystems.t9t.translation.request.GetEnumsTranslationRequest
import com.arvatosystems.t9t.translation.request.GetEnumsTranslationResponse
import com.arvatosystems.t9t.translation.services.ITranslationProvider
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import java.util.ArrayList
import com.arvatosystems.t9t.init.InitContainers
import com.arvatosystems.t9t.base.T9tException
import de.jpaw.util.ApplicationException

@AddLogger
class GetEnumsTranslationRequestHandler extends AbstractReadOnlyRequestHandler<GetEnumsTranslationRequest> {

    @Inject protected ITranslationProvider translationProvider

    def protected EnumTranslationDTO translateEnum(RequestContext ctx, String pqon, String language, Boolean useFallback) {
        val result = new EnumTranslationDTO => [
            enumPQON                = pqon
        ]
//        try {
//            LOGGER.debug("Looking for translations of enum {} for language {}", pqon, language)
//            val enumClass = Class.forName("com.arvatosystems." + pqon)
//            val enumInstances = enumClass.enumConstants
//            if (enumInstances === null) {
//                LOGGER.warn("EnumTranslationDTO called for {}, which is no enum", pqon)
//                result.instances           = #[]  // no ENUM. Should we raise an Exception?
//            } else {
//                // was an enum
//                result.instances           = new ArrayList<EnumInstanceDTO>(enumInstances.length)
//                val xlates = translationProvider.getEnumTranslation(ctx.tenantId, pqon, language, useFallback === null || useFallback.booleanValue)
//                for (enInst : enumInstances) {
//                    val name = (enInst as Enum).name
//                    result.instances.add(new EnumInstanceDTO(name, xlates?.get(name) ?: name))
//                }
//            }
//        } catch (Exception e) {
//            LOGGER.error("EnumTranslationDTO called for {}, which cannot be found", pqon)
//            result.instances               = #[]
//        }
        val enumDef = InitContainers.getEnumByPQON(pqon)
        if (enumDef === null)
            throw new ApplicationException(T9tException.NOT_AN_ENUM, pqon)
        val xlates = translationProvider.getEnumTranslations(ctx.tenantId, language, useFallback === null || useFallback.booleanValue, pqon, enumDef.ids)
        result.instances = new ArrayList<EnumInstanceDTO>(enumDef.ids.length)
        for (var int i = 0; i < enumDef.ids.length; i += 1)
            result.instances.add(new EnumInstanceDTO(enumDef.ids.get(i), xlates.get(i) ?: enumDef.ids.get(i)))
        return result
    }

    override GetEnumsTranslationResponse execute(RequestContext ctx, GetEnumsTranslationRequest rq) {
        val results = new ArrayList<EnumTranslationDTO>(rq.enumPQONs.size)

        val language = rq.overrideLanguage ?: ctx.internalHeaderParameters.jwtInfo.locale ?: "en"
        for (en: rq.enumPQONs) {
            results.add(ctx.translateEnum(en, language, rq.useFallback))
        }

        return new GetEnumsTranslationResponse => [
            translations = results
        ]
    }
}
