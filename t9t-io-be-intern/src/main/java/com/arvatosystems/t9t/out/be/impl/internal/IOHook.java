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
package com.arvatosystems.t9t.out.be.impl.internal;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.cfg.be.CipherFactory;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.services.IIOHook;

import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

@Singleton
public class IOHook implements IIOHook {
    private static final int DEFAULT_BUFFER_SIZE_GZIP = 512;
    private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;  // according to docs, sizes > 8 KB may actually have a negative impact

    @Override
    public InputStream getDecompressionStream(final InputStream is, final DataSinkDTO config, final int bufferSize) {
        // the implementation varies depending on if compressed is used or not, because GZIP input streams use internal buffering,
        // both should not be combined
        if (config.getCompressed()) {
            try {
                return new GZIPInputStream(is, bufferSize > 2 ? bufferSize : DEFAULT_BUFFER_SIZE_GZIP);
            } catch (final IOException e) {
                throw new T9tException(T9tIOException.INPUT_FILE_GUNZIP_EXCEPTION);
            }
        } else {
            return new BufferedInputStream(is, bufferSize > 2 ? bufferSize : DEFAULT_BUFFER_SIZE);
        }
    }

    @Override
    public OutputStream getCompressionStream(final OutputStream os, final DataSinkDTO config) {
        try {
            return config.getCompressed() ? new GZIPOutputStream(os) : os;
        } catch (final IOException e) {
            throw new T9tException(T9tIOException.OUTPUT_FILE_GZIP_EXCEPTION);
        }
    }

    @Override
    public InputStream getDecryptionStream(@Nonnull final InputStream is, @Nonnull final DataSinkDTO config) {
        if (config.getEncryptionId() == null) {
            return is;
        } else {
            final Cipher cipher = CipherFactory.getCipher(config.getEncryptionId(), Cipher.DECRYPT_MODE, null);
            return new CipherInputStream(is, cipher);
        }
    }

    @Override
    public OutputStream getEncryptionStream(@Nonnull final OutputStream os, @Nonnull final DataSinkDTO config) {
        if (config.getEncryptionId() != null) {
            final Cipher cipher = CipherFactory.getCipher(config.getEncryptionId(), Cipher.ENCRYPT_MODE, null);
            return new CipherOutputStream(os, cipher);
        } else {
            return os;
        }
    }
}
