package com.arvatosystems.t9t.tfi.web.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.tfi.web.security.JwtAuthenticationToken;
import com.arvatosystems.t9t.tfi.web.security.JwtUtils;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

public class JwtRealm extends AuthorizingRealm implements ICacheableAuthorizationRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRealm.class);
    CommonRealm commonRealm;

    public JwtRealm() {
        super(new MemoryConstrainedCacheManager());
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token != null && token instanceof JwtAuthenticationToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        JwtAuthenticationToken upToken = (JwtAuthenticationToken) token;
        JwtInfo jwtInfo = JwtUtils.getJwtPayload(upToken.getToken());
        AuthenticationInfo info = new SimpleAccount(jwtInfo.getUserId(), upToken.getCredentials(), getName());
        return info;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        LOGGER.debug("========================= JwtRealm AuthorizationInfo=========================");
        String userName = (String) getAvailablePrincipal(principals);
        return commonRealm.getAuthorizationInfo(userName);
    }

    @Override
    public void clearAuthorizationCache() {
        clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }

}
