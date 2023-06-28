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
package com.arvatosystems.t9t.auth.be.impl;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IExternalAuthentication;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.LdapConfiguration;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/** This implementation provides LDAP authentication. */
@Singleton
public class ExternalAuthentication implements IExternalAuthentication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalAuthentication.class);

    protected final IAuthPersistenceAccess persistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);

    @Override
    public AuthIntermediateResult externalAuth(RequestContext ctx, PasswordAuthentication pw, DataWithTrackingS<UserDTO, FullTrackingWithVersion> user) {
        // the default provider required the user to exist in our local DB (for permissions)
        final AuthIntermediateResult resp = new AuthIntermediateResult();
        resp.setTenantId(user.getTenantId());
        resp.setUser(user.getData());

        try {
            final boolean success = authenticateJndi(pw.getUserId(), pw.getPassword());
            resp.setReturnCode(success ? 0 : T9tException.WRONG_PASSWORD);
        } catch (Exception e) {
            LOGGER.error("Authentication exception: ", e);
            resp.setReturnCode(T9tException.GENERAL_AUTH_PROBLEM);
        }
        return resp;
    }

    protected boolean authenticateJndi(String username, String password) throws Exception {
        LdapConfiguration ldapConfiguration = ConfigProvider.getConfiguration().getLdapConfiguration();  // we know it is not null, because we are called
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getContextFactory()); // "com.sun.jndi.ldap.LdapCtxFactory"
        props.put(Context.PROVIDER_URL,            ldapConfiguration.getProviderUrl());
        props.put(Context.SECURITY_PRINCIPAL,      ldapConfiguration.getSecurityPrincipal()); // "uid=adminuser,ou=special users,o=xx.com");
        props.put(Context.SECURITY_CREDENTIALS,    ldapConfiguration.getSecurityCredentials());

        InitialDirContext context = new InitialDirContext(props);

        SearchControls ctrls = new SearchControls();
        ctrls.setReturningAttributes(new String[] { "givenName", "sn", "memberOf" });
        ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        NamingEnumeration<javax.naming.directory.SearchResult> answers = context.search(ldapConfiguration.getOrganization(), "(uid=" + username + ")", ctrls);
        javax.naming.directory.SearchResult result = answers.nextElement();

        String user = result.getNameInNamespace();

        try {
            props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfiguration.getContextFactory());
            props.put(Context.PROVIDER_URL,            ldapConfiguration.getProviderUrl());
            props.put(Context.SECURITY_PRINCIPAL,      user);
            props.put(Context.SECURITY_CREDENTIALS,    password);

            context = new InitialDirContext(props);
            // FIXME: context is not used?
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
