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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;

public abstract class AbstractTextFormatConverter extends AbstractInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTextFormatConverter.class);

    public abstract void process(String textLine);

    @Override
    public void process(final InputStream is) {
        final DataSinkDTO cfg = inputSession.getDataSinkDTO();

        // avoid the need for duplicate test to null or ""
        final Object singleLineComment = cfg.getSingleLineComment() == null || cfg.getSingleLineComment().isBlank() ? null : cfg.getSingleLineComment();
        int linesToSkip = cfg.getLinesToSkip() == null ? 0 : cfg.getLinesToSkip();
        final InputStreamReader isr;
        if (cfg.getOutputEncoding() == null) {
            LOGGER.debug("Opening text input stream for data sink {} with default encoding UTF-8", cfg.getDataSinkId());
            isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        } else {
            try {
                LOGGER.debug("Opening text input stream for data sink {} with encoding {}", cfg.getDataSinkId(), cfg.getOutputEncoding());
                isr = new InputStreamReader(is, cfg.getOutputEncoding());
            } catch (final UnsupportedEncodingException usee) {
                LOGGER.error("Failed to use encoding {} for data sink {}: {}", cfg.getOutputEncoding(), cfg.getDataSinkId(), usee);
                throw new T9tException(T9tIOException.IO_EXCEPTION, "Encoding " + cfg.getOutputEncoding());
            }
        }
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
        } catch (IOException e) {
            LOGGER.error("Error when reading line from input stream.", e);
            throw new T9tException(T9tIOException.IO_EXCEPTION);
        } finally {
            try {
                streamReader.close();
            } catch (IOException f) {
                // should (hopefully) never happen because its already caught beforehand.
            }
        }
    }
}
