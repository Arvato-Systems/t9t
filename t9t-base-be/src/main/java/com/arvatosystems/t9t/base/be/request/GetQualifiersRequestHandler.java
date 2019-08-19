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
package com.arvatosystems.t9t.base.be.request;

import java.util.HashSet;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.request.GetQualifiersRequest;
import com.arvatosystems.t9t.base.request.GetQualifiersResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

/**
 * Retrieves the interface or abstract super class by reflection and returns all implementations of it.
 */
public class GetQualifiersRequestHandler extends AbstractReadOnlyRequestHandler<GetQualifiersRequest> {

    @Override
    public GetQualifiersResponse execute(GetQualifiersRequest rq) {
        final GetQualifiersResponse resp = new GetQualifiersResponse();
        resp.setQualifiers(new HashSet<>());
        try {
            // perform lookup for a list of classes
            for (String fqcn: rq.getFullyQualifiedClassNames()) {
                Class<?> type = Class.forName(fqcn);
                resp.getQualifiers().addAll(Jdp.getQualifiers(type));
            }
        } catch (Exception e) {
            throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, ExceptionUtil.causeChain(e));
        }
        return resp;
    }
}
