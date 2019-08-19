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
package com.arvatosystems.t9t.auth.jwt;

/*
 * Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.Mac;

/**
 * Internal common interface for all crypto algorithms. This is just an utility in order to simplify sign and verify operations.
 *
 * @author Paulo Lopes
 */
public interface Crypto {
    byte[] sign(byte[] payload) throws Exception;

    boolean verify(byte[] signature, byte[] payload) throws Exception;
}

/**
 * MAC based Crypto implementation
 *
 * @author Paulo Lopes
 */
final class CryptoMac implements Crypto {
    private final Mac mac;

    CryptoMac(final Mac mac) {
        this.mac = mac;
    }

    @Override
    public byte[] sign(byte[] payload) {
        return mac.doFinal(payload);
    }

    @Override
    public boolean verify(byte[] signature, byte[] payload) {
        return Arrays.equals(signature, mac.doFinal(payload));
    }
}

/**
 * Signature based Crypto implementation
 *
 * @author Paulo Lopes
 */
final class CryptoSignature implements Crypto {
    private final Signature sig;
    private final PrivateKey privateKey;
    private final X509Certificate certificate;

    CryptoSignature(final X509Certificate certificate, final PrivateKey privateKey) throws NoSuchAlgorithmException {
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.sig = Signature.getInstance(certificate.getSigAlgName());
    }

    @Override
    public byte[] sign(byte[] payload) throws SignatureException, InvalidKeyException {
        sig.initSign(privateKey);
        sig.update(payload);
        return sig.sign();
    }

    @Override
    public boolean verify(byte[] signature, byte[] payload) throws InvalidKeyException, SignatureException {
        sig.initVerify(certificate);
        sig.update(payload);
        return sig.verify(signature);
    }
}
