package com.arvatosystems.t9t.zkui.azure.ad;

import com.arvatosystems.t9t.zkui.util.UiConfigurationProvider;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

public final class AadConstants {

    private AadConstants() {
    }

    public static final long STATE_TTL                = 10; // in minutes
    public static final String SESSION_PARAM          = "aadParam";
    public static final String POST_SIGN_OUT_FRAGMENT = "?post_logout_redirect_uri=";
    public static final String REDIRECT_ENDPOINT      = "/aadRedirect.zul";

    private static String getWithDefault(final String key) {
        final String valueByPropertyFile = UiConfigurationProvider.getProperty(key);
        return valueByPropertyFile != null ? valueByPropertyFile : ZulUtils.readConfig(key);
    }

    public static final String  MICROSOFT_AUTH_ENABLED_STR = getWithDefault("login.enableMicrosoftAuth");
    public static final boolean MICROSOFT_AUTH_ENABLED     = "true".equals(MICROSOFT_AUTH_ENABLED_STR);

    public static final String SIGN_OUT_ENDPOINT = getWithDefault("azure.ad.signOutEndPoint");
    public static final String SECRET            = getWithDefault("azure.ad.secret");
    public static final String CLIENT_ID         = getWithDefault("azure.ad.clientId");
    public static final String AUTHORITY         = getWithDefault("azure.ad.authority");
    public static final String SCOPES            = getWithDefault("azure.ad.scopes");
    public static final String HOME_PAGE         = getWithDefault("azure.ad.mainPage");

    public static final String REDIRECT_URI = HOME_PAGE + REDIRECT_ENDPOINT;
}
