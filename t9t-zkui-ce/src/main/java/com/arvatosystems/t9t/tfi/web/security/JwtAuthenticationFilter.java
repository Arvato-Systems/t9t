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
package com.arvatosystems.t9t.tfi.web.security;

import java.util.List;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;

public class JwtAuthenticationFilter extends AuthenticatingFilter {

    public static String TOKEN_PARAM_ID = "token";

    protected final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) throws Exception {
        if (isJwtExisted(request, response)) {
            String jwtToken = getAuthorizationHeader(request);
            if (jwtToken != null) {
                return createJwtAuthenticationToken(jwtToken);
            }
        }
        return null;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        boolean loggedIn = false;
        if (isJwtExisted(request, response)) {
            loggedIn = executeLogin(request, response);
        }

        if (!loggedIn) {
            HttpServletResponse httpResponse = WebUtils.toHttp(response);
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return loggedIn;
    }

    protected boolean isJwtExisted(ServletRequest request, ServletResponse response) {
        String authzHeader = getAuthorizationHeader(request);
        return authzHeader != null;
    }

    protected String getAuthorizationHeader(ServletRequest request) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        return httpRequest.getParameter(TOKEN_PARAM_ID);
    }

    public JwtAuthenticationToken createJwtAuthenticationToken(String token) {
        JwtInfo jwtInfo = JwtUtils.getJwtPayload(token);
        return new JwtAuthenticationToken(jwtInfo.getUserId(), token);
    }

}
