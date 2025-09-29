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
package com.arvatosystems.t9t.hs;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

/**
 * This class contains all exception codes used in hibernate search module.
 */
public class T9tHibernateSearchException extends T9tException {
    private static final long serialVersionUID = -71286196848412432L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = T9tConstants.EXCEPTION_OFFSET_HIBERNATE_SEARCH;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    public static final int HIBERNATE_SEARCH_CONFIG_NOT_FOUND      = OFFSET + 1;
    public static final int HIBERNATE_SEARCH_INVALID_CONFIG        = OFFSET + 2;
    public static final int DOCUMENT_ENTITY_CLASS_NOT_FOUND        = OFFSET + 3;
    public static final int SEARCH_FILTER_CONVERTER_NOT_FOUND      = OFFSET + 4;

    static {
        registerRange(CORE_OFFSET, false, T9tHibernateSearchException.class, ApplicationLevelType.FRAMEWORK, "t9t hibernate search module");

        registerCode(HIBERNATE_SEARCH_CONFIG_NOT_FOUND,      "Hibernate Search configuration not found");
        registerCode(HIBERNATE_SEARCH_INVALID_CONFIG,        "Hibernate Search configuration is invalid");
        registerCode(DOCUMENT_ENTITY_CLASS_NOT_FOUND,        "Entity class not found for document");
        registerCode(SEARCH_FILTER_CONVERTER_NOT_FOUND,      "Converter for search filter not found");
    }
}
