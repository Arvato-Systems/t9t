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
package com.arvatosystems.t9t.email.be.smtp.impl;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import jakarta.activation.DataSource;

public class MediaDataSource implements DataSource {
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

    @Override
    public String getContentType() {
        return MediaTypeInfo.getFormatByType(data.getMediaType()).getMimeType();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (data.getText() != null)
            return new ByteArrayInputStream(data.getText().getBytes(StandardCharsets.UTF_8));
        else
            return data.getRawData().asByteArrayInputStream();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("Cannot write to MediaDataSource");
    }
}
