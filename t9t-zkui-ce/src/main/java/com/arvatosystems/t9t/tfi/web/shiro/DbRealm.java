/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.tfi.web.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.util.ExceptionUtil;

/**
 *
 * @author INCI02
 *
 */
public class DbRealm extends AuthorizingRealm implements ICacheableAuthorizationRealm {
    private static final String LOGIN_FAILED_MESSAGE = "Login failed";
    private static final Logger LOGGER               = LoggerFactory.getLogger(AuthorizingRealm.class);

    //@Inject
    CommonRealm commonRealm;

    public DbRealm() {
        super(new MemoryConstrainedCacheManager());

    }

    /**
     * @param token
     *            AuthenticationToken
     * @return AuthenticationInfo
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        LOGGER.debug("=========================DbRealm AuthenticationInfo Token" + token + "=========================");

        AuthenticationInfo info;
        try {
            info = commonRealm.getAuthenticationInfo(token, getName());
        } catch (AuthenticationException e) {
            LOGGER.warn("DATABASE authentication failed: {}", ExceptionUtil.causeChain(e));
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("*** ONLY RELEVANT FOR DEVELOPMENT");
                throw new AuthenticationException(LOGIN_FAILED_MESSAGE, e);
            }
            return null;
        } catch (Exception e) {
            LOGGER.warn("DB Login failed. {}", ExceptionUtil.causeChain(e));
            throw new AuthenticationException(LOGIN_FAILED_MESSAGE, e);
        }

        return info;
    }

    /**
     * @param principals
     *            PrincipalCollection
     * @return AuthorizationInfo
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        LOGGER.debug("========================= DbRealm AuthorizationInfo=========================");
        String userName = (String) getAvailablePrincipal(principals);
        return commonRealm.getAuthorizationInfo(userName);
    }

    /**
     * @return the commonRealm
     */
    public CommonRealm getCommonRealm() {
        return commonRealm;
    }

    /**
     * @param commonRealm the commonRealm to set
     */
    public void setCommonRealm(CommonRealm commonRealm) {
        this.commonRealm = commonRealm;
    }

    @Override
    public void clearAuthorizationCache() {
        clearCachedAuthorizationInfo(SecurityUtils.getSubject().getPrincipals());
    }
}
