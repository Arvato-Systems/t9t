/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc;

import com.arvatosystems.t9t.doc.api.DocumentSelector;

public final class DocConstants {
    private DocConstants() { }

    public static final String DEFAULT_LANGUAGE_CODE = "xx";
    public static final String DEFAULT_COUNTRY_CODE  = "XX";
    public static final String DEFAULT_CURRENCY_CODE = "XXX";
    public static final String DEFAULT_ENTITY_ID     = "-";

    public static final DocumentSelector GENERIC_DOCUMENT_SELECTOR = new DocumentSelector();
    static {
        GENERIC_DOCUMENT_SELECTOR.setCountryCode(DEFAULT_COUNTRY_CODE);
        GENERIC_DOCUMENT_SELECTOR.setCurrencyCode(DEFAULT_CURRENCY_CODE);
        GENERIC_DOCUMENT_SELECTOR.setLanguageCode(DEFAULT_LANGUAGE_CODE);
        GENERIC_DOCUMENT_SELECTOR.setEntityId(DEFAULT_ENTITY_ID);
        GENERIC_DOCUMENT_SELECTOR.freeze();
    }
}
