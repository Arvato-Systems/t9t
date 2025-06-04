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

import java.util.HashMap;
import java.util.Map;

import com.arvatosystems.t9t.base.request.QueryConfigRequest;
import com.arvatosystems.t9t.base.request.QueryConfigResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import de.jpaw.bonaparte.util.FreezeTools;

/**
 * A request handler to query backend configuration values.
 */
public class QueryConfigRequestHandler extends AbstractReadOnlyRequestHandler<QueryConfigRequest> {
    private static final String SECURITY_PREFIX = "PUBLIC_";  // a prefix of all settings which are accessible (to avoid retrieving sensitive information)

    @Override
    public QueryConfigResponse execute(final RequestContext ctx, final QueryConfigRequest rq) {
        final Map<String, String> mappings = new HashMap<>(FreezeTools.getInitialHashMapCapacity(rq.getVariables().size()));
        final QueryConfigResponse resp = new QueryConfigResponse();
        resp.setKeyValuePairs(mappings);
        for (String variable: rq.getVariables()) {
            final String value = ConfigProvider.getCustomParameter(SECURITY_PREFIX + variable);
            if (value != null) {
                mappings.put(variable, value);
            }
        }
        return resp;
    }
}
