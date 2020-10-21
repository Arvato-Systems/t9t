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
package com.arvatosystems.t9t.tfi.viewmodel;

import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginHelper.class);
    private static final String SSL_CLIENT_VERIFY = "ssl_client_verify";
    private static final String SSL_CLIENT_S_DN = "ssl_client_s_dn";
    public static final String ADDITIONAL_PARAM = "additionalParam";
    public static final String TOKEN_BUTTON = "btnLoginToken";

    public static String getSubjectDNHeader(ServletRequest request) {
        String userName = null;
        //String subjectDN = null;
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = WebUtils.toHttp(request);

            String subjectDNHeader = req.getHeader(SSL_CLIENT_S_DN);

            //"SUCCESS";
            //req.getHeader(SSL_CLIENT_VERIFY);
            String verifiedHeader = req.getHeader(SSL_CLIENT_VERIFY);

            //String additionalParam = req.getParameter(ADDITIONAL_PARAM);

            if (notEmpty(verifiedHeader) && notEmpty(subjectDNHeader) /*&& notEmpty(additionalParam)*/) {
                if ("SUCCESS".equals(verifiedHeader) /*&& ((null == additionalParam) || TOKEN_BUTTON.equals(additionalParam))*/
                        && (subjectDNHeader.indexOf("CN=") != -1)) {
                    userName = subjectDNHeader.substring(subjectDNHeader.indexOf("CN=") + 3);
                } else {
                    //throw new AuthenticationException( "Client certificate verification failure was forwarded" );
                    userName = null;

                }
            }

        }
        return userName;

    }

    public static boolean isRealmExisting(String realmToCheck) {
        Collection<Realm> configuredRealms = ((RealmSecurityManager) SecurityUtils.getSecurityManager()).getRealms();
        for (Realm realm : configuredRealms) {

            if (realm.getName().equals(realmToCheck)) {
                return true;
            }
        }
        return false;
    }

    private static boolean notEmpty(String str) {
        return (str != null) && (str.length() > 1);
    }

    public static void logRequestInfo(ServletRequest request) {
        if (request instanceof HttpServletRequest && LOGGER.isDebugEnabled()) {
            HttpServletRequest req = WebUtils.toHttp(request); // (HttpServletRequest) request;
            Enumeration<String> headerNames = req.getHeaderNames();

            LOGGER.debug("=====================================HEADER INFO=====================================");
            while (headerNames.hasMoreElements()) {

                String headerName = headerNames.nextElement();
                LOGGER.debug("------------------------------ {} ----------------------", headerName);

                Enumeration<String> headers = req.getHeaders(headerName);
                while (headers.hasMoreElements()) {
                    String headerValue = headers.nextElement();
                    LOGGER.debug("--> {}", headerValue);
                }

            }

            /*
            LOGGER.debug("=====================================REQUEST PARAM INFO=====================================");
            Enumeration<String> parameterNames = req.getParameterNames();
            while (parameterNames.hasMoreElements()) {

                String paramName = parameterNames.nextElement();
                LOGGER.debug("------------------------------ {} ----------------------", paramName);

                String[] paramValues = req.getParameterValues(paramName);
                for (int i = 0; i < paramValues.length; i++) {
                    String paramValue = paramValues[i];
                    LOGGER.debug("--> {}", paramValue);
                }

            }
             */
        }
    }
}
