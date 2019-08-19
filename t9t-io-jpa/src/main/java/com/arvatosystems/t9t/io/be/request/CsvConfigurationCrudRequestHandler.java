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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.io.CsvConfigurationDTO;
import com.arvatosystems.t9t.io.CsvConfigurationRef;
import com.arvatosystems.t9t.io.jpa.entities.CsvConfigurationEntity;
import com.arvatosystems.t9t.io.jpa.mapping.ICsvConfigurationDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ICsvConfigurationEntityResolver;
import com.arvatosystems.t9t.io.request.CsvConfigurationCrudRequest;

import de.jpaw.dp.Jdp;

public class CsvConfigurationCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<CsvConfigurationRef, CsvConfigurationDTO, FullTrackingWithVersion, CsvConfigurationCrudRequest, CsvConfigurationEntity> {

//  @Inject
    private final ICsvConfigurationEntityResolver sinksResolver = Jdp.getRequired(ICsvConfigurationEntityResolver.class);

//  @Inject
    private final ICsvConfigurationDTOMapper sinksMapper = Jdp.getRequired(ICsvConfigurationDTOMapper.class);

    @Override
    public ServiceResponse execute(CsvConfigurationCrudRequest crudRequest) throws Exception {
        return execute(sinksMapper, sinksResolver, crudRequest);
    }
}
