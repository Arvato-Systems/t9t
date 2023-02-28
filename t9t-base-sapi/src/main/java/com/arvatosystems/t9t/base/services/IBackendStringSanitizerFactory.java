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
package com.arvatosystems.t9t.base.services;

import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;

/** This class is responsible for creation of the parameter sanitizer for the backend. */
public interface IBackendStringSanitizerFactory {
    /** Creates an instance of the string sanitizer, based on configuration in config.xml. */
    DataConverter<String, AlphanumericElementaryDataItem> createStringSanitizerForBackend();

    /**
     * Creates an instance of the string sanitizer, based on given parameters.
     *
     * @param forbiddenCharacters the forbidden chars which will be replaced by {@code replacementCharacter}
     * @param replacementCharacter the replacement char to be used (if passing 'null', it will fallback to '?')
     *
     * @return a {@ link DataConverter} to be used for sanitizing
     */
    DataConverter<String, AlphanumericElementaryDataItem> createCommonStringSanitizer(String forbiddenCharacters, String replacementCharacter);
}
