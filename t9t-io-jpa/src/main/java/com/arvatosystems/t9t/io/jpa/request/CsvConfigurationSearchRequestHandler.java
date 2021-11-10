/*
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
import com.arvatosystems.t9t.io.CsvConfigurationDTO;
import com.arvatosystems.t9t.io.jpa.entities.CsvConfigurationEntity;
import com.arvatosystems.t9t.io.jpa.mapping.ICsvConfigurationDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ICsvConfigurationEntityResolver;
import com.arvatosystems.t9t.io.request.CsvConfigurationSearchRequest;

import de.jpaw.dp.Jdp;

public class CsvConfigurationSearchRequestHandler extends
        AbstractSearch42RequestHandler<Long, CsvConfigurationDTO, FullTrackingWithVersion, CsvConfigurationSearchRequest, CsvConfigurationEntity> {

    protected final ICsvConfigurationEntityResolver resolver = Jdp.getRequired(ICsvConfigurationEntityResolver.class);
    protected final ICsvConfigurationDTOMapper mapper = Jdp.getRequired(ICsvConfigurationDTOMapper.class);

    @Override
    public ReadAllResponse<CsvConfigurationDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
            final CsvConfigurationSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
