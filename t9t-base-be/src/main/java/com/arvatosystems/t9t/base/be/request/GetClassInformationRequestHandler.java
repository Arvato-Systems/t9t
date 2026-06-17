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
package com.arvatosystems.t9t.base.be.request;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;

import com.arvatosystems.t9t.base.request.GetClassInformationRequest;
import com.arvatosystems.t9t.base.request.GetClassInformationResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class GetClassInformationRequestHandler extends AbstractReadOnlyRequestHandler<GetClassInformationRequest> {

    @Override
    public GetClassInformationResponse execute(final RequestContext ctx, final GetClassInformationRequest request) throws Exception {
        final BonaPortableClass<?> bclass = BonaPortableFactory.getBClassForPqon(request.getPqon());
        final ClassDefinition cd = bclass.getMetaData();
        final GetClassInformationResponse resp = new GetClassInformationResponse();
        resp.setClassDefinition(cd);
        return resp;
    }
}
