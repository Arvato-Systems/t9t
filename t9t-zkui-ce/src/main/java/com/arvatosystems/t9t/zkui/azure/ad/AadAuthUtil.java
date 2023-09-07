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
