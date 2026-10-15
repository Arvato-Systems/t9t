package com.arvatosystems.t9t.api.auth;

import java.util.UUID;

import de.jpaw.util.ApplicationException;

public interface IAuthenticationBackend {
    JwtInfo authByApiKey(UUID apiKey) throws ApplicationException;
    JwtInfo authByUserPassword(String userId, String password) throws ApplicationException;
}
