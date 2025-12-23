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

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.AbstractSessionInvalidationRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSessionInvalidationRequestHandler<R extends AbstractSessionInvalidationRequest> extends AbstractRequestHandler<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSessionInvalidationRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final R request) {
        final boolean clusterMode = T9tUtil.isTrue(ConfigProvider.getConfiguration().getRunInCluster());
        if (request.getExecuteOnCurrentNode() || !clusterMode) {
            performInvalidation(request);
        } else {
            final AbstractSessionInvalidationRequest clusteredRequest = request.ret$MutableClone(true, false);
            clusteredRequest.setExecuteOnCurrentNode(true);
            LOGGER.debug("Dispatching session invalidation request to all cluster nodes. {}", clusteredRequest);
            executor.executeOnEveryNode(ctx, clusteredRequest);
        }
        return ok();
    }

    protected abstract void performInvalidation(@Nonnull R request);
}
