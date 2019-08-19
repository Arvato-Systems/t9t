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
package com.arvatosystems.t9t.doc.be.impl

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.types.Recipient
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.services.IDocUnknownDistributor
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.dp.Fallback
import de.jpaw.dp.Singleton
import java.util.function.Function

// class should be overridden to add more types of Recipients
@Singleton
@Fallback
class DocUnknownDistributor implements IDocUnknownDistributor {
    override transmit(Recipient rcpt, Function<MediaXType, MediaData> data, MediaXType primaryFormat, String documentTemplateId, DocumentSelector documentSelector) {
        throw new T9tException(T9tException.ILLEGAL_REQUEST_PARAMETER, "recipient type " + rcpt.ret$PQON + " not recognized by distribution")
    }
}
