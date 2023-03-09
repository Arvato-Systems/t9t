/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.auth.T9tAuthTools;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.auth.request.UserCrudRequest;
import com.arvatosystems.t9t.auth.services.IUserResolver;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;

public class UserCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<UserRef, UserDTO, FullTrackingWithVersion, UserCrudRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserCrudRequestHandler.class);
    private static final String FORBIDDEN_CHARS_USER_ID = "${}[]()/\\^~=!";

    private final IUserResolver resolver = Jdp.getRequired(IUserResolver.class);
    private final IAuthCacheInvalidation cacheInvalidator = Jdp.getRequired(IAuthCacheInvalidation.class);

    @Override
    public CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final UserCrudRequest crudRequest) {
        if (crudRequest.getNaturalKey() instanceof UserKey key) {
            checkForAllowedUserId(key.getUserId());
        }
        final UserDTO userDto = crudRequest.getData();
        if (userDto != null) {
            checkForAllowedUserId(userDto.getUserId());
            // also limit the min / max permissions
            final JwtInfo jwt = ctx.internalHeaderParameters.getJwtInfo();
            T9tAuthTools.maskPermissions(userDto.getPermissions(), jwt.getPermissionsMax());
        }
        final CrudSurrogateKeyResponse<UserDTO, FullTrackingWithVersion> result = execute(ctx, crudRequest, resolver);
        if (crudRequest.getCrud() != OperationType.READ) {
            final String userId = userDto != null ? userDto.getUserId() : null;
            cacheInvalidator.invalidateAuthCache(ctx, UserDTO.class.getSimpleName(), result.getKey(), userId);
        }
        return result;
    }

    private void checkForAllowedUserId(final String id) {
        if ("$init".equals(id)) {
            // exception (for historic reasons)
            return;
        }
        if (id.contains("..") || id.isEmpty() || id.charAt(0) == '.' || id.charAt(id.length() - 1) == '.') {
            LOGGER.error("Attempted to create / update to invalid user ID (with double dots or dot at bad position) {}", id);
            throw new T9tException(T9tAuthException.INVALID_USER_ID);
        }
        for (int i = 0; i < id.length(); ++i) {
            final char c = id.charAt(i);
            if (FORBIDDEN_CHARS_USER_ID.indexOf(c) >= 0) {
                LOGGER.error("Attempted to create / update to invalid user ID (illegal character) {}", id);
                throw new T9tException(T9tAuthException.INVALID_USER_ID);
            }
        }
    }
}
