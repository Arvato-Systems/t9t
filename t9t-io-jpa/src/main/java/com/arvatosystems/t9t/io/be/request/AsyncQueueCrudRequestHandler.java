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
package com.arvatosystems.t9t.io.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.AsyncQueueRef;
import com.arvatosystems.t9t.io.jpa.entities.AsyncQueueEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncQueueDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncQueueEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncQueueCrudRequest;
import com.arvatosystems.t9t.out.services.IAsyncQueue;

import de.jpaw.dp.Jdp;

public class AsyncQueueCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<AsyncQueueRef, AsyncQueueDTO, FullTrackingWithVersion, AsyncQueueCrudRequest, AsyncQueueEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueueCrudRequestHandler.class);
    protected final IAsyncQueueEntityResolver resolver  = Jdp.getRequired(IAsyncQueueEntityResolver.class);
    protected final IAsyncQueueDTOMapper      mapper    = Jdp.getRequired(IAsyncQueueDTOMapper.class);
    protected final IAsyncQueue               queueImpl = Jdp.getRequired(IAsyncQueue.class);

    @Override
    public CrudSurrogateKeyResponse<AsyncQueueDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final AsyncQueueCrudRequest request) {
        final CrudSurrogateKeyResponse<AsyncQueueDTO, FullTrackingWithVersion> resp = execute(ctx, mapper, resolver, request);
        final AsyncQueueDTO data = resp.getData();

        switch (request.getCrud()) {
        case ACTIVATE:
            LOGGER.info("Activating queue {}", data.getAsyncQueueId());
            ctx.addPostCommitHook((context, rp, res) -> queueImpl.open(data));
            break;
        case CREATE:
            if (data.getIsActive()) {
                LOGGER.info("Activating new queue {}", data.getAsyncQueueId());
                ctx.addPostCommitHook((context, rp, res) -> queueImpl.open(data));
            }
            break;
        case DELETE:
            LOGGER.info("Shutting down queue");
            ctx.addPostCommitHook((context, rp, res) -> queueImpl.close(resp.getKey()));
            break;
        case INACTIVATE:
            LOGGER.info("Shutting down queue");
            ctx.addPostCommitHook((context, rp, res) -> queueImpl.close(data.getObjectRef()));
            break;
        case MERGE:
        case UPDATE:
            ctx.addPostCommitHook((context, rp, res) -> {
                queueImpl.close(data.getObjectRef());
            });
            if (data.getIsActive()) {
                LOGGER.info("Restarting queue {}", data.getAsyncQueueId());
                ctx.addPostCommitHook((context, rp, res) -> queueImpl.open(data));
            }
            break;
        default:
            break;

        }
        return resp;
    }
}
