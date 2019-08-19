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
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.io.jpa.mapping.ISinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.SinkSearchRequest;

import de.jpaw.dp.Jdp;

public class SinkSearchRequestHandler extends AbstractSearchRequestHandler<SinkSearchRequest> {

//  @Inject
    private final ISinkEntityResolver sinksResolver = Jdp.getRequired(ISinkEntityResolver.class);
//  @Inject
    private final ISinkDTOMapper sinksMapper = Jdp.getRequired(ISinkDTOMapper.class);

    @Override
    public ServiceResponse execute(SinkSearchRequest request) throws Exception {
        sinksMapper.processSearchPrefixForDB(request);
        return sinksMapper.createReadAllResponse(sinksResolver.search(request, null), request.getSearchOutputTarget());
    }
}
