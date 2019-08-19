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
package com.arvatosystems.t9t.event.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.impl.EventSubscriptionCache;
import com.arvatosystems.t9t.event.SubscriberConfigDTO;
import com.arvatosystems.t9t.event.SubscriberConfigRef;
import com.arvatosystems.t9t.event.jpa.entities.SubscriberConfigEntity;
import com.arvatosystems.t9t.event.jpa.mapping.ISubscriberConfigDTOMapper;
import com.arvatosystems.t9t.event.jpa.persistence.ISubscriberConfigEntityResolver;
import com.arvatosystems.t9t.event.request.SubscriberConfigCrudRequest;

import de.jpaw.dp.Jdp;

/**
 * @author BORUS01
 *
 */
public class SubscriberConfigCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<SubscriberConfigRef, SubscriberConfigDTO, FullTrackingWithVersion, SubscriberConfigCrudRequest, SubscriberConfigEntity> {

    private final ISubscriberConfigEntityResolver entityResolver = Jdp.getRequired(ISubscriberConfigEntityResolver.class);
    private final ISubscriberConfigDTOMapper sinksMapper = Jdp.getRequired(ISubscriberConfigDTOMapper.class);
    private final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriberConfigCrudRequestHandler.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, SubscriberConfigCrudRequest crudRequest) throws Exception {
        SubscriberConfigDTO dto = crudRequest.getData();
        if (dto != null && dto.getHandlerClassName().indexOf(".") < 0) {
            // some CRUD request with data
            switch (crudRequest.getCrud()) {
            case CREATE:
            case MERGE:
            case UPDATE:
                LOGGER.debug("Received {} CRUD request for SubscriberConfig.",crudRequest.getCrud());
                ctx.addPostCommitHook((ctx2, rq, rs) -> {
                    IEventHandler eventHandler = Jdp.getOptional(IEventHandler.class, dto.getHandlerClassName());
                    if (eventHandler != null) {
                        asyncProcessor.registerSubscriber(dto.getEventID(), ctx.tenantRef, eventHandler);
                        EventSubscriptionCache.updateRegistration(dto.getEventID(), dto.getHandlerClassName(), ctx.tenantRef, dto.getIsActive());
                    }
                    else {
                        LOGGER.error("Can't find eventHandler with name '{}' to register for event id={}. Skipping this SubscriberConfig entry.",dto.getHandlerClassName(),dto.getEventID());
                        throw new T9tException(T9tException.INVALID_CONFIGURATION, "Can't find event handler with that name!");
                    }
                });
                break;
            default:
                break;
            }
            // TODO: update for activate / deactivate / delete as well
        }
        return execute(sinksMapper, entityResolver, crudRequest);
    }
}
