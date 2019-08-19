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
package com.arvatosystems.t9t.doc.services;

import java.util.Map;

import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.TemplateType;

import de.jpaw.bonaparte.pojos.api.media.MediaData;

public interface IDocFormatter {
    MediaData formatDocument(
        Long sharedTenantRef,
        TemplateType templateType,
        String template,
        DocumentSelector selector,
        String overrideTimeZone,
        Object data,
        Map<String, MediaData> cidMap // if null, the binary data will be inlined in HTML without conversion, otherwise stored here
    );
}
