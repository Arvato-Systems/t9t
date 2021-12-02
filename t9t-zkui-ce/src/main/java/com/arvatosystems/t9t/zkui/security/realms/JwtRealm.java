/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.security.realms;

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

import com.arvatosystems.t9t.zkui.security.JwtAuthenticationToken;
import com.arvatosystems.t9t.zkui.util.JwtUtils;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

public class JwtRealm extends AuthorizingRealm implements ICacheableAuthorizationRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtRealm.class);
    private CommonRealm commonRealm;

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
