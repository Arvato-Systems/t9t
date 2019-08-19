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
package com.arvatosystems.t9t.out.be.impl.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.out.be.ISpecificTranslationProvider;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;

import de.jpaw.bonaparte.enums.BonaTokenizableEnum;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;

/**
 * Holds the generic translation provider, but each instance of this class holds the tenant and language as well, in order to avoid passing lots of parameters
 * when this provider is transferred.
 *
 */
public class SpecificTranslationProvider implements ISpecificTranslationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificTranslationProvider.class);
    private final ITranslationProvider translationProvider;
    private final String tenantId;
    private final String language;

    // boilerplate code...
    public SpecificTranslationProvider(ITranslationProvider translationProvider, String tenantId, String language) {
        this.translationProvider = translationProvider;
        this.tenantId = tenantId;
        this.language = language;
    }

    @Override
    public String translateEnum(EnumDataItem di, BonaTokenizableEnum n) {
        String translation = translationProvider.getEnumTranslation(n, tenantId, language);

        if (translation == null) {
            LOGGER.warn("Missing enum translation. class: {}, symbol: {}, tenantId: {}, language: {}", n.getClass(), n, tenantId, language);
            return n.getToken(); // fallback
        } else {
            return translation;
        }
    }
}
