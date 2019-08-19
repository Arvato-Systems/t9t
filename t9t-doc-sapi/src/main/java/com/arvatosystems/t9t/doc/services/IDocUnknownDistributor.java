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

import java.util.function.Function;

import com.arvatosystems.t9t.base.types.Recipient;
import com.arvatosystems.t9t.doc.api.DocumentSelector;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;

public interface IDocUnknownDistributor {
    void transmit(
        Recipient rcpt,
        Function<MediaXType, MediaData> data,
        MediaXType primaryFormat,
        String documentTemplateId,      // the unmapped template ID
        DocumentSelector documentSelector
    );
}
