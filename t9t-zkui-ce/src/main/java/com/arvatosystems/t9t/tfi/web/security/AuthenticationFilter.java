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
package com.arvatosystems.t9t.tfi.web.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import com.arvatosystems.t9t.tfi.viewmodel.LoginHelper;

/**
 * AuthenticationFilter.
 *
 * @author INCI02
 *
 */
public class AuthenticationFilter extends FormAuthenticationFilter {
    /**
     * @param request ServletRequest
     * @param ae AuthenticationException
     */
    @Override
    protected final void setFailureAttribute(ServletRequest request, AuthenticationException ae) {
        String message = ae.getMessage();
        request.setAttribute(getFailureKeyAttribute(), message);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        LoginHelper.logRequestInfo(request);
        String username = getUsername(request);
        String password = getPassword(request);
        return createToken(username, password, request, response);
    }
}
