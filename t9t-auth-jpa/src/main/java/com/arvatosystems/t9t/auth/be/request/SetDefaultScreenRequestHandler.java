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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.SetDefaultScreenRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class SetDefaultScreenRequestHandler extends AbstractRequestHandler<SetDefaultScreenRequest> {

    protected final IUserEntityResolver resolver = Jdp.getRequired(IUserEntityResolver.class);
    private final Logger LOGGER = LoggerFactory.getLogger(SetDefaultScreenRequestHandler.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, SetDefaultScreenRequest request) throws Exception {
        UserEntity userEntity = resolver.find(ctx.userRef);
        userEntity.setDefaultScreenId(request.getDefaultScreenId());
        LOGGER.info("set default screen as {} to user {}", request.getDefaultScreenId(), userEntity.getObjectRef());
        return ok();
    }
}
