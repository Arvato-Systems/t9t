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
package com.arvatosystems.t9t.io.services;

import java.io.InputStream;
import java.io.OutputStream;

import com.arvatosystems.t9t.io.DataSinkDTO;

import jakarta.annotation.Nonnull;

/**
 * Provides an interface for encryption or decryption.
 */
public interface IIOHook {

    /**
     * Returns a wrapper which decompresses data, if configured, or returns a buffered stream, if that is configured.
     */
    InputStream getDecompressionStream(@Nonnull InputStream is, @Nonnull DataSinkDTO config, int bufferSize);

    /**
     * Returns a wrapper which compresses data, if configured.
     */
    OutputStream getCompressionStream(@Nonnull OutputStream os, @Nonnull DataSinkDTO config);

    /**
     * Returns a wrapper which decrypts data, if configured.
     */
    InputStream getDecryptionStream(@Nonnull InputStream is, @Nonnull DataSinkDTO config);

    /**
     * Returns a wrapper which encrypts data, if configured.
     */
    OutputStream getEncryptionStream(@Nonnull OutputStream os, @Nonnull DataSinkDTO config);
}
