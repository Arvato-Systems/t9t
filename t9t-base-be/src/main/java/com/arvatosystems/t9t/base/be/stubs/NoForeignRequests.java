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
package com.arvatosystems.t9t.base.be.stubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Any
@Singleton
public class NoForeignRequests implements IForeignRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoForeignRequests.class);

    public NoForeignRequests() {
        LOGGER.warn("NoForeignRequests implementation selected - cannot call out upstream");
    }

    @Override
    public ServiceResponse execute(RequestContext ctx, RequestParameters rp) {
        LOGGER.error("Rejecting foreign call out");
        ServiceResponse resp = new ServiceResponse();
        resp.setReturnCode(T9tException.NOT_YET_IMPLEMENTED);
        resp.setErrorDetails("NOOP callout: " + rp.ret$PQON());
        return resp;
    }
}
