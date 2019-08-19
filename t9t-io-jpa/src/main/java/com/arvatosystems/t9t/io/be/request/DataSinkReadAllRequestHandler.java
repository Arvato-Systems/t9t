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
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.request.DataSinkReadAllRequest;

import de.jpaw.dp.Jdp;

public class DataSinkReadAllRequestHandler extends AbstractRequestHandler<DataSinkReadAllRequest> {

//  @Inject
    private final IDataSinkEntityResolver sinksResolver = Jdp.getRequired(IDataSinkEntityResolver.class);

//  @Inject
    private final IDataSinkDTOMapper sinksMapper = Jdp.getRequired(IDataSinkDTOMapper.class);

    @Override
    public ServiceResponse execute(DataSinkReadAllRequest request) throws Exception {
        ReadAllResponse<DataSinkDTO, FullTrackingWithVersion> rs = new ReadAllResponse<DataSinkDTO, FullTrackingWithVersion>();
        rs.setDataList(sinksMapper.mapListToDwt(sinksResolver.readAll(request.getReturnOnlyActive())));
        rs.setReturnCode(0);
        return rs;
    }
}
