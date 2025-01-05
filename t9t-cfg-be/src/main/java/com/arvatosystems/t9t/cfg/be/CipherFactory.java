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
package com.arvatosystems.t9t.cfg.be;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public final class CipherFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CipherFactory.class);

    private CipherFactory() { }

    /** Creates an instance of the Cipher class, for encryption or decryption. */
    public static Cipher getCipher(@Nonnull final String id, final int mode,  @Nullable byte[] ivData) {
        final EncryptionConfiguration cfg = ConfigProvider.getEncryptionOrThrow(id);
        final Cipher cipher;
        try {
            if (cfg.getProvider() == null) {
                cipher = Cipher.getInstance(cfg.getTransformation());
            } else {
                cipher = Cipher.getInstance(cfg.getTransformation(), cfg.getProvider());
            }
        } catch (final NoSuchAlgorithmException e) {
            LOGGER.error("ID {}: No such cipher: {}: {}", id, cfg.getTransformation(), e.getMessage());
            throw new T9tException(T9tException.ENCRYPTION_NO_SUCH_ALGORITHM, cfg.getTransformation());
        } catch (final NoSuchProviderException e) {
            LOGGER.error("ID {}: No such provider: {}: {}", id, cfg.getProvider(), e.getMessage());
            throw new T9tException(T9tException.ENCRYPTION_NO_SUCH_PROVIDER, cfg.getProvider());
        } catch (final NoSuchPaddingException e) {
            LOGGER.error("ID {}: No such padding: {}: {}", id, cfg.getTransformation(), e.getMessage());
            throw new T9tException(T9tException.ENCRYPTION_NO_SUCH_PADDING, cfg.getTransformation());
        }
        // cipher instance has been constructed

        // create the secret key
        final byte[] secretKeyBytes;
        if (Boolean.TRUE.equals(cfg.getSecretKeyIsBase64())) {
            secretKeyBytes = Base64.getDecoder().decode(cfg.getSecretKeyData());
        } else {
            secretKeyBytes = cfg.getSecretKeyData().getBytes(StandardCharsets.UTF_8);
        }
        final SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, cfg.getSecretKeyAlgorithm());
        // secret key has been constructed

        try {
            if (cfg.getGcmTLen() == null) {
                cipher.init(mode, secretKey);
            } else {
                // GCM
                final int ivLen = cfg.getIvLen();
                if (ivLen < 32 || ivLen > 512) {
                    // the plausi check even allows for some future enhancements...
                    LOGGER.error("ID {}: ivLen {} is not plausible, plausible lengths are 128, 120, 112, 104, 96, or 64 or 32", id, cfg.getIvLen());
                    throw new T9tException(T9tException.ENCRYPTION_BAD_IV_LENGTH, cfg.getIvLen());
                }
                if (ivData == null || ivData.length < ivLen) {
                    throw new T9tException(T9tException.ENCRYPTION_NO_IV_DATA, id);
                }
                final GCMParameterSpec gcmSpec = new GCMParameterSpec(cfg.getGcmTLen(), ivData, 0, ivLen);
                cipher.init(mode, secretKey, gcmSpec);
            }
        } catch (final InvalidKeyException e) {
            LOGGER.error("ID {}: Invalid secret key for {}: {}", id, cfg.getSecretKeyAlgorithm(), e.getMessage());
            throw new T9tException(T9tException.ENCRYPTION_INVALID_KEY, id + ":" + cfg.getSecretKeyAlgorithm());
        } catch (final InvalidAlgorithmParameterException e) {
            throw new T9tException(T9tException.ENCRYPTION_BAD_PARAMETER, id);
        }
        return cipher;
    }

    /** Decrypts the given data, using the encryption specified by the id. */
    public static byte[] decrypt(@Nonnull final String id, @Nonnull final byte[] encryptedData) {
        final Cipher cipher = getCipher(id, Cipher.DECRYPT_MODE, encryptedData);
        // check for implicit IV
        final byte[] iv = cipher.getIV();
        final int ivLen = iv == null ? 0 : iv.length;
        try {
            return cipher.doFinal(encryptedData, ivLen, encryptedData.length - ivLen);
        } catch (final Exception e) {
            LOGGER.error("ID {}: Decryption failed: {}", id, e.getMessage());
            throw new T9tException(T9tException.ENCRYPTION_DECRYPTION_FAILED, id);
        }
    }

    /** Encrypts the given data, using the encryption specified by the id. */
    public static byte[] encrypt(@Nonnull final String id, @Nonnull final byte[] data, @Nullable final byte[] iv) {
        final Cipher cipher = getCipher(id, Cipher.ENCRYPT_MODE, iv);
        try {
            return cipher.doFinal(data);
        } catch (final Exception e) {
            LOGGER.error("ID {}: Encryption failed: {}", id, e.getMessage());
            throw new T9tException(T9tException.ENCRYPTION_ENCRYPTION_FAILED, id);
        }
    }
}
