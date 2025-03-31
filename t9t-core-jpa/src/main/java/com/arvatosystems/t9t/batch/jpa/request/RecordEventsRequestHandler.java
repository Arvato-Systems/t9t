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
package com.arvatosystems.t9t.batch.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.jpa.entities.RecordEventsEntity;
import com.arvatosystems.t9t.batch.jpa.persistence.IRecordEventsEntityResolver;
import com.arvatosystems.t9t.batch.request.RecordEventsRequest;
import de.jpaw.dp.Jdp;

public class RecordEventsRequestHandler extends AbstractRequestHandler<RecordEventsRequest> {

    private final IRecordEventsEntityResolver entityResolver = Jdp.getRequired(IRecordEventsEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RecordEventsRequest rq) {
        final RecordEventsEntity entity = new RecordEventsEntity();

        entity.setObjectRef(entityResolver.createNewPrimaryKey());
        entity.setEventSource(rq.getEventSource());
        entity.setEventSeverity(rq.getEventSeverity());
        entity.setId1(rq.getId1());
        entity.setId2(rq.getId2());
        entity.setStatus(rq.getStatus());
        entity.setStatusMessage(rq.getStatusMessage());

        this.entityResolver.save(entity);
        return ok();
    }
}
