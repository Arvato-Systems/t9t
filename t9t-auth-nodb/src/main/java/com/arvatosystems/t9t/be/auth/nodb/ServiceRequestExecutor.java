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
package com.arvatosystems.t9t.be.auth.nodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.dp.Singleton;

@Singleton
public class ServiceRequestExecutor implements IUnauthenticatedServiceRequestExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRequestExecutor.class);

    @Override
    public ServiceResponse execute(final ServiceRequest srq) {
        return execute(srq, false);
    }

    @Override
    public ServiceResponse executeTrusted(final ServiceRequest srq) {
        return execute(srq, true);
    }

    protected ServiceResponse execute(final ServiceRequest srq, final boolean isTrusted) {
        LOGGER.debug("Execute {} for {}", isTrusted ? "TRUSTED" : "untrusted", srq.getRequestParameters().ret$PQON());
        throw new RuntimeException("not implemented");
    }
}
