/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.UpdateUserDataRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class UpdateUserDataRequestHandler extends AbstractRequestHandler<UpdateUserDataRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateUserDataRequestHandler.class);

    private final IUserEntityResolver userResolver = Jdp.getRequired(IUserEntityResolver.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final UpdateUserDataRequest request) {
        LOGGER.debug("Updating basic data for user {}", ctx.userId);
        final UserEntity userEntity = userResolver.getEntityDataForKey(ctx.userRef);
        userEntity.setName(request.getName());
        userEntity.setFirstName(request.getFirstName());
        userEntity.setPhoneNo(request.getPhoneNo());
        userEntity.setMobilePhoneNo(request.getMobilePhoneNo());
        userResolver.save(userEntity);
        return ok();
    }
}
