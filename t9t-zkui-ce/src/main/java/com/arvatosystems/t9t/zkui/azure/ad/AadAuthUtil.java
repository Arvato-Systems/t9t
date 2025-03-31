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
package com.arvatosystems.t9t.zkui.azure.ad;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IClientSecret;

public final class AadAuthUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AadAuthUtil.class);

    private AadAuthUtil() {
    }

    public static ConfidentialClientApplication getConfidentialClientInstance() throws MalformedURLException {
        LOGGER.trace("AAD auth getting confidential client instance");
        final IClientSecret secret = ClientCredentialFactory.createFromSecret(AadConstants.SECRET);
        return ConfidentialClientApplication.builder(AadConstants.CLIENT_ID, secret).authority(AadConstants.AUTHORITY).build();
    }

}
