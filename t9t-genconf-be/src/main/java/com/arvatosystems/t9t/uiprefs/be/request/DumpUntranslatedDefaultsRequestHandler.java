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
package com.arvatosystems.t9t.uiprefs.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.uiprefs.request.DumpUntranslatedDefaultsRequest;

public class DumpUntranslatedDefaultsRequestHandler extends AbstractRequestHandler<DumpUntranslatedDefaultsRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DumpUntranslatedDefaultsRequestHandler.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, DumpUntranslatedDefaultsRequest request) throws Exception {
        StringBuilder buff = new StringBuilder(10000);
        for (String s: GridConfigRequestHandler.UNTRANSLATED_DEFAULTS.keySet()) {
            buff.append(s);
            buff.append("=\n");
        }
        LOGGER.info("Untranslated defaults are:\n{}", buff.toString());
        return ok();
    }
}
