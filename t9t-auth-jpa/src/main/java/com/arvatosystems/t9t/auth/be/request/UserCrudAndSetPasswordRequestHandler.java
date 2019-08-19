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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.auth.jpa.IPasswordSettingService;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class UserCrudAndSetPasswordRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<UserRef, UserDTO, FullTrackingWithVersion, UserCrudAndSetPasswordRequest, UserEntity> {
    protected final IUserEntityResolver resolver = Jdp.getRequired(IUserEntityResolver.class);
    protected final IUserDTOMapper mapper = Jdp.getRequired(IUserDTOMapper.class);
    protected final IPasswordSettingService passwordSettingService = Jdp.getRequired(IPasswordSettingService.class);

    @Override
    public CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> execute(RequestContext ctx, final UserCrudAndSetPasswordRequest request) {

        // check for allowed subset of return codes
        switch (request.getCrud()) {
        case CREATE:
        case UPDATE:
        case MERGE:
            break;  // OK
        default:
            throw new ApplicationException(T9tException.INVALID_CRUD_COMMAND, "only CREATE, UPDATE and MERGE allowed here");
        }
        CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> resp = super.execute(mapper, resolver, request);

        if (resp.getReturnCode() == 0) {
            // perform the setting or update of the password
            UserEntity userEntity = resolver.getEntityManager().find(UserEntity.class, resp.getKey());
            passwordSettingService.setPasswordForUser(ctx, userEntity, request.getPassword());
        }
        return resp;
    }
}
