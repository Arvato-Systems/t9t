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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.request.LogJdbcPoolRequest;
import com.arvatosystems.t9t.base.request.LogJdbcPoolResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class LogJdbcPoolRequestHandler extends AbstractReadOnlyRequestHandler<LogJdbcPoolRequest> {

    private final IJdbcConnectionProvider jdbcProvider = Jdp.getRequired(IJdbcConnectionProvider.class, "independent");

    @Override
    public LogJdbcPoolResponse execute(final RequestContext ctx, final LogJdbcPoolRequest rq) {
        final LogJdbcPoolResponse resp = new LogJdbcPoolResponse();
        resp.setCounts(jdbcProvider.checkHealth());
        return resp;
    }
}
