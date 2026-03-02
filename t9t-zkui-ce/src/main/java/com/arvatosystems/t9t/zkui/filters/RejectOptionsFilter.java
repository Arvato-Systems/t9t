/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.filters;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Rejects HTTP OPTIONS requests early.
 * <p>
 * ZK UI endpoints (especially ZK AU requests) don't need OPTIONS and in some
 * deployment setups it can be abused for probing.
 */
public class RejectOptionsFilter implements Filter {

    @Override
    public void init(final FilterConfig filterConfig) {
        // no init
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (request instanceof final HttpServletRequest httpReq && response instanceof final HttpServletResponse httpResp) {
            final String method = httpReq.getMethod();
            if (method != null && method.equalsIgnoreCase("OPTIONS")) {
                httpResp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing
    }
}
