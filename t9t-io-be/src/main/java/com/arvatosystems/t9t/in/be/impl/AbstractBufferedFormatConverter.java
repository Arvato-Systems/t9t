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
package com.arvatosystems.t9t.in.be.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.T9tIOException;

import jakarta.annotation.Nonnull;

/**
 * This class implements buffered reading and input decompression.
 */
public abstract class AbstractBufferedFormatConverter extends AbstractInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBufferedFormatConverter.class);
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;  // according to docs, sizes > 8 KB may actually have a negative impact
    private static final int DEFAULT_BUFFER_SIZE_GZIP = 512;

    public abstract void processBuffered(@Nonnull InputStream is);

    @Override
    public final void process(final InputStream is) {
        // assign buffer size - anything less than 2 will disable buffering
        final int bufferSize = importDataSinkDTO.getBufferSize() != null ? importDataSinkDTO.getBufferSize() : DEFAULT_BUFFER_SIZE;

        try {
            // the implementation varies depending on if compressed is used or not, because GZIP input streams use internal buffering,
            // both should not be combined
            if (importDataSinkDTO.getCompressed()) {
                processBuffered(new GZIPInputStream(is, bufferSize > 2 ? bufferSize : DEFAULT_BUFFER_SIZE_GZIP));
            } else {
                processBuffered(new BufferedInputStream(is, bufferSize > 2 ? bufferSize : DEFAULT_BUFFER_SIZE));
            }
        } catch (final IOException e) {
            LOGGER.error("Error when reading line from input stream.", e);
            throw new T9tException(T9tIOException.IO_EXCEPTION, importDataSinkDTO.getDataSinkId());
        } finally {
            try {
                is.close();
            } catch (final IOException f) {
                // should (hopefully) never happen because its already caught beforehand.
            }
        }
    }
}
