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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.AutonomousTransactionRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class AutonomousTransactionRequestHandler extends AbstractRequestHandler<AutonomousTransactionRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutonomousTransactionRequestHandler.class);

    private final IAutonomousExecutor autoExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final AutonomousTransactionRequest rq) {
        LOGGER.debug("Launching request {} in a different thread", rq.getRequest().ret$PQON());
        return autoExecutor.execute(ctx, rq.getRequest(), false);
    }
}
