package com.arvatosystems.t9t.auth.services;

import org.eclipse.xtext.xbase.lib.Pair;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.services.RequestContext;

/** Authentication via external providers (LDAP, Active Directory...). */
public interface IExternalAuthentication {
    /**
     * Request external authentication for this user.
     */
    AuthIntermediateResult externalAuth(RequestContext ctx, PasswordAuthentication pw, Pair<Long, UserDTO> user);
}
