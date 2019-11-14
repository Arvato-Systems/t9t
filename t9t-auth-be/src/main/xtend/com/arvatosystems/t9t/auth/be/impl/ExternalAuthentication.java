package com.arvatosystems.t9t.auth.be.impl;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import org.eclipse.xtext.xbase.lib.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IExternalAuthentication;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.PasswordAuthentication;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.LdapConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/** This implementation provides LDAP authentication. */
@Singleton
public class ExternalAuthentication implements IExternalAuthentication {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalAuthentication.class);

    protected final IAuthPersistenceAccess persistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);

    @Override
    public AuthIntermediateResult externalAuth(RequestContext ctx, PasswordAuthentication pw) {
        final Pair<Long, UserDTO> user = persistenceAccess.getUserById(pw.getUserId());
        // the default provider required the user to exist in our local DB (for permissions)
        if (user == null) {
            throw new T9tException(T9tException.USER_NOT_FOUND);
        }
        final AuthIntermediateResult resp = new AuthIntermediateResult();
        resp.setTenantRef(user.getKey());
        resp.setUser(user.getValue());

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
        props.put(Context.SECURITY_PRINCIPAL,      ldapConfiguration.getSecurityPrincipal()); // "uid=adminuser,ou=special users,o=xx.com");//adminuser - User with special priviledge, dn user
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
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
