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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.GetDefaultScreenRequest;
import com.arvatosystems.t9t.auth.request.GetDefaultScreenResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class GetDefaultScreenRequestHandler extends AbstractRequestHandler<GetDefaultScreenRequest> {

    protected final IUserEntityResolver resolver = Jdp.getRequired(IUserEntityResolver.class);

    @Override
    public GetDefaultScreenResponse execute(final RequestContext ctx, final GetDefaultScreenRequest request) throws Exception {
        final UserEntity userEntity = resolver.find(ctx.userRef);
        final GetDefaultScreenResponse response = new GetDefaultScreenResponse();
        response.setDefaultScreenId(userEntity.getDefaultScreenId());
        return response;
    }
}
