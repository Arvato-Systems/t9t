/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.services;

import java.util.List;

import de.jpaw.bonaparte.pojos.api.media.EnumOutputType;

/** Immutable data class, mostly boilerplate code. */
public class FoldableParams {
    private final List<String> selectedFields;
    private final List<String> headers;
    private final EnumOutputType relevantEnumType;
    private final ISpecificTranslationProvider enumTranslator;
    private final boolean applyVariantFilter;

    public FoldableParams(final List<String> selectedFields, final List<String> headers,
            final EnumOutputType relevantEnumType, final ISpecificTranslationProvider enumTranslator,
            final boolean applyVariantFilter) {
        super();
        this.selectedFields = selectedFields;
        this.headers = headers;
        this.relevantEnumType = relevantEnumType;
        this.enumTranslator = enumTranslator;
        this.applyVariantFilter = applyVariantFilter;
    }

    public List<String> getSelectedFields() {
        return this.selectedFields;
    }

    public List<String> getHeaders() {
        return this.headers;
    }

    public EnumOutputType getRelevantEnumType() {
        return this.relevantEnumType;
    }

    public ISpecificTranslationProvider getEnumTranslator() {
        return this.enumTranslator;
    }

    public boolean isApplyVariantFilter() {
        return this.applyVariantFilter;
    }
}
