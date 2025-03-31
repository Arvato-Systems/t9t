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
package com.arvatosystems.t9t.in.be.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.io.T9tIOException;

public abstract class AbstractTextFormatConverter extends AbstractBufferedFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTextFormatConverter.class);

    public abstract void process(String textLine);

    @Override
    public void processBuffered(final InputStream is) {
        // avoid the need for duplicate test to null or ""
        final Object singleLineComment = T9tUtil.isBlank(importDataSinkDTO.getSingleLineComment()) ? null : importDataSinkDTO.getSingleLineComment();
        int linesToSkip = importDataSinkDTO.getLinesToSkip() == null ? 0 : importDataSinkDTO.getLinesToSkip();
        LOGGER.debug("Opening text input stream for data sink {} with encoding {}", importDataSinkDTO.getDataSinkId(), importCharset.name());
        final InputStreamReader isr = new InputStreamReader(is, importCharset);
        final BufferedReader streamReader = new BufferedReader(isr);
        try {
            for (String line = streamReader.readLine(); line != null; line = streamReader.readLine()) {
                if (singleLineComment == null || !line.startsWith(singleLineComment.toString())) {
                    if (linesToSkip > 0) {
                        linesToSkip--;
                    } else {
                        process(line);
                    }
                }
            }
        } catch (final IOException e) {
            LOGGER.error("Error when reading line from input stream.", e);
            throw new T9tException(T9tIOException.IO_EXCEPTION);
        } finally {
            try {
                streamReader.close();
            } catch (final IOException f) {
                // should (hopefully) never happen because its already caught beforehand.
            }
        }
    }
}
