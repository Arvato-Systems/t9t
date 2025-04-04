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
package com.arvatosystems.t9t.email.be.vertx.impl;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import io.vertx.core.buffer.Buffer;
import java.util.Map;

public class MediaDataSource {
    private final MediaData data;
    private final String name;

    private static String defaultName(final Map<String, Object> z) {
        final String cid = z == null ? null : (String) z.get("cid");
        return cid;
    }

    public MediaDataSource(final MediaData data) {
        this.data = data;
        this.name = defaultName(data.getZ()); // ?: "X" + Integer.toString(counter.incrementAndGet)
    }

    public String getContentType() {
        return MediaTypeInfo.getFormatByType(data.getMediaType()).getMimeType();
    }

    public Buffer asBuffer() {
        if (data.getText() != null) {
            return Buffer.buffer(data.getText());
        }
        return Buffer.buffer(data.getRawData().getBytes()); // duplicate copy, unfortunately
    }

    public String getName() {
        return name;
    }
}
