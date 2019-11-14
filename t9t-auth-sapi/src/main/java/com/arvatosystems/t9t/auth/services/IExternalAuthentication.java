package com.arvatosystems.t9t.auth.services;

import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.services.RequestContext;

/** Authentication via external providers (LDAP, Active Directory...). */
public interface IExternalAuthentication {
    /**
     * Request external authentication for this user.
     *
     * @param user
     * @param password
     * @return
     */
    AuthIntermediateResult externalAuth(RequestContext ctx, PasswordAuthentication pw);
}
