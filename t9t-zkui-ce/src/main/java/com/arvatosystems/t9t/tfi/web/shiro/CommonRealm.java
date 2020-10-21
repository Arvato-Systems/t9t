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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.auth.PermissionEntry;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
/**
 * Common Realm.
 * @author INCI02
 *
 */
@Singleton
public class CommonRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRealm.class);

    protected final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    /**
     *
     * @param token AuthenticationToken
     * @param authenticationType String
     * @param name String
     * @return AuthenticationInfo
     */
    protected AuthenticationInfo getAuthenticationInfo(AuthenticationToken token, String name) {
        LOGGER.debug("========================= getAuthenticationInfo =========================");
        AuthenticationInfo info;
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;

        String username = upToken.getUsername();
        LOGGER.debug("Authenticating user '{}'", username);

        String pwd = new String(upToken.getPassword());

        AuthenticationResponse authResponse = null;
        try {
            authResponse = userDAO.getAuthenticationResponse(username, pwd);
        } catch (ReturnCodeException e) {
//            if ((authResponse == null) ||
//                    ((authResponse.getReturnCode() != Constants.ErrorCodes.RETURN_CODE_SUCCESS) && (authResponse.getReturnCode() != T9tException.PASSWORD_EXPIRED))) {
            LOGGER.warn("May Missing object data for AllowedTenants/UserHistory/UserInformation");
            throw new UnknownAccountException("Login failed");
        }

        info = new SimpleAccount(username, upToken.getPassword(), name);
        return info;
    }

    /**
     *
     * @param userName String
     * @return AuthorizationInfo
     */
    protected AuthorizationInfo getAuthorizationInfo(String userName) {
        LOGGER.debug("========================= getAuthorizationInfo =========================");

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        try {
            info.setStringPermissions(convertToSet(userDAO.getPermissions()));
            info.setRoles(new HashSet<String>());
            return info;
        } catch (ReturnCodeException e) {
            // if error return empty SimpleAuthorizationInfo
            return info;
        }
    }

    private Set<String> convertToSet(Collection<PermissionEntry> elements) {
        HashSet<String> returnHashSet=new HashSet<String>();
        if (elements == null) {
            return returnHashSet;
        }

        for (PermissionEntry temp : elements) {
            returnHashSet.add(temp.getResourceId());
        }
        return returnHashSet;
    }
}
