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

import java.io.IOException;
import java.io.InputStream;

import com.arvatosystems.t9t.io.services.IIOHook;

import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;

/**
 * This class implements buffered reading and input decompression.
 */
public abstract class AbstractBufferedFormatConverter extends AbstractInputFormatConverter {

    protected final IIOHook ioHook = Jdp.getOptional(IIOHook.class);

    public abstract void processBuffered(@Nonnull InputStream is);

    @Override
    public final void process(final InputStream is) {
        // assign buffer size - anything less than 2 will disable buffering
        final int bufferSize = importDataSinkDTO.getBufferSize() != null ? importDataSinkDTO.getBufferSize() : 0;

        // if data is encrypted, decrypt it first
        final InputStream isUnEncrypted;
        if (ioHook == null) {
            isUnEncrypted = is;
        } else {
            isUnEncrypted = ioHook.getDecompressionStream(ioHook.getDecryptionStream(is, importDataSinkDTO), importDataSinkDTO, bufferSize);
        }
        processBuffered(isUnEncrypted);
        try {
            is.close();
        } catch (final IOException f) {
            // should (hopefully) never happen because its already caught beforehand.
        }
    }
}
