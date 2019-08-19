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
package com.arvatosystems.t9t.email.be.vertx.impl

import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.pojos.api.media.MediaData
import io.vertx.core.buffer.Buffer
import java.util.Map

class MediaDataSource {
    private final MediaData data
    private final String name

    def private static String defaultName(Map<String, Object> z) {
        val cid = z?.get("cid")
        if (cid !== null && cid instanceof String)
            return cid as String
        return null
    }

    new(MediaData data) {
        this.data = data
        this.name = defaultName(data.z) //?: "X" + Integer.toString(counter.incrementAndGet)
    }

    def getContentType() {
        return MediaTypeInfo.getFormatByType(data.mediaType).mimeType
    }

    def Buffer asBuffer() {
        if (data.text !== null)
            return Buffer.buffer(data.text)
        else
            return Buffer.buffer(data.rawData.bytes)  // duplicate copy, unfortunately
    }

    def getName() {
        return name
    }
}
