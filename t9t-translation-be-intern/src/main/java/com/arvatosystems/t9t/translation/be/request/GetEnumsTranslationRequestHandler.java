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
package com.arvatosystems.t9t.translation.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.init.InitContainers;
import com.arvatosystems.t9t.translation.request.EnumInstanceDTO;
import com.arvatosystems.t9t.translation.request.EnumTranslationDTO;
import com.arvatosystems.t9t.translation.request.GetEnumsTranslationRequest;
import com.arvatosystems.t9t.translation.request.GetEnumsTranslationResponse;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.dp.Jdp;
import java.util.ArrayList;
import java.util.List;

public class GetEnumsTranslationRequestHandler extends AbstractReadOnlyRequestHandler<GetEnumsTranslationRequest> {

    protected final ITranslationProvider translationProvider = Jdp.getRequired(ITranslationProvider.class);

    @Override
    public GetEnumsTranslationResponse execute(final RequestContext ctx, final GetEnumsTranslationRequest request) throws Exception {
        final List<EnumTranslationDTO> results = new ArrayList<>(request.getEnumPQONs().size());
        String language = request.getOverrideLanguage();
        if (language == null) {
            language = ctx.internalHeaderParameters.getJwtInfo().getLocale();
        }
        if (language == null) { // still null
            language = "en";
        }
        for (String en : request.getEnumPQONs()) {
            results.add(translateEnum(ctx, en, language, request.getUseFallback()));
        }
        final GetEnumsTranslationResponse response = new GetEnumsTranslationResponse();
        response.setTranslations(results);
        return response;
    }

    protected EnumTranslationDTO translateEnum(final RequestContext ctx, final String pqon, final String language, final Boolean useFallback) {
        final EnumTranslationDTO result = new EnumTranslationDTO(pqon, null);
        final EnumDefinition enumDef = InitContainers.getEnumByPQON(pqon);
        if (enumDef == null) {
            throw new T9tException(T9tException.NOT_AN_ENUM, pqon);
        }
        final boolean tryFallbackLanguages = useFallback == null || useFallback.booleanValue();
        final List<String> xlates = translationProvider.getEnumTranslations(ctx.tenantId, language, tryFallbackLanguages, pqon, enumDef.getIds());
        result.setInstances(new ArrayList<>(enumDef.getIds().size()));
        for (int i = 0; i < enumDef.getIds().size(); i += 1) {
            final String enumInstanceTranslation = xlates.get(i) == null ? enumDef.getIds().get(i) : xlates.get(i);
            final EnumInstanceDTO enumInstance = new EnumInstanceDTO(enumDef.getIds().get(i), enumInstanceTranslation);
            result.getInstances().add(enumInstance);
        }
        return result;
    }

}
