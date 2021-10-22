/**
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.request.DataSinkSearchRequest;
import de.jpaw.dp.Jdp;

public class DataSinkSearchRequestHandler extends
        AbstractSearch42RequestHandler<Long, DataSinkDTO, FullTrackingWithVersion, DataSinkSearchRequest, DataSinkEntity> {

    protected final IDataSinkEntityResolver resolver = Jdp.getRequired(IDataSinkEntityResolver.class);
    protected final IDataSinkDTOMapper mapper = Jdp.getRequired(IDataSinkDTOMapper.class);

    @Override
    public ReadAllResponse<DataSinkDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
            final DataSinkSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
