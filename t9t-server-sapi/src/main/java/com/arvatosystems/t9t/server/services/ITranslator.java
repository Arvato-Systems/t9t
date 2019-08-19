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
package com.arvatosystems.t9t.server.services;

import java.util.List;

import com.arvatosystems.t9t.base.trns.TextCategory;
import com.arvatosystems.t9t.base.trns.TranslationsPartialKey;

/** Cross module access for the translations module. Only these APIs are used (maybe only one of both). */
public interface ITranslator {
    List<String> getTranslations(String languageCode, TextCategory category, List<String> qualifiedIds);
    List<String> getTranslations(String languageCode, List<TranslationsPartialKey> keys);
}
