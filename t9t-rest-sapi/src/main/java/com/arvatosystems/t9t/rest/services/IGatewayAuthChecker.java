package com.arvatosystems.t9t.rest.services;

import com.arvatosystems.t9t.base.types.AuthenticationParameters;

public interface IGatewayAuthChecker {
    /** Checks if the authentication info is acceptable for an API call. */
    boolean isValidAuth(String authHeader, AuthenticationParameters authParams);
}
