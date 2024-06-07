/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.batch.RecordEventsDTO;
import com.arvatosystems.t9t.batch.RecordEventsRef;
import com.arvatosystems.t9t.batch.jpa.entities.RecordEventsEntity;
import com.arvatosystems.t9t.batch.request.RecordEventsCrudRequest;

public class RecordEventsCrudRequestHandler
extends AbstractCrudSurrogateKeyRequestHandler<RecordEventsRef, RecordEventsDTO, WriteTracking, RecordEventsCrudRequest, RecordEventsEntity> {

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RecordEventsCrudRequest crudRequest) throws Exception {
        return execute(ctx, crudRequest);
    }
}
