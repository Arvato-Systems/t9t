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
package com.arvatosystems.t9t.auth.jpa.request;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.ITenantDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantEntityResolver;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudStringKeyRequestHandler;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;

public class TenantCrudRequestHandler extends AbstractCrudStringKeyRequestHandler<TenantDTO, FullTrackingWithVersion, TenantCrudRequest, TenantEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantCrudRequestHandler.class);
    private static final Pattern ALLOWED_TENANT_ID_PATTERN = Pattern.compile("[-_A-Za-z0-9]*");

    private final ITenantEntityResolver resolver = Jdp.getRequired(ITenantEntityResolver.class);
    private final ITenantDTOMapper mapper = Jdp.getRequired(ITenantDTOMapper.class);
    private final IAuthCacheInvalidation cacheInvalidator = Jdp.getRequired(IAuthCacheInvalidation.class);

    @Override
    public CrudStringKeyResponse<TenantDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final TenantCrudRequest crudRequest) {
        if (crudRequest.getKey() != null) {
            checkForAllowedTenantId(crudRequest.getKey());
        }
        if (crudRequest.getData() != null) {
            checkForAllowedTenantId(crudRequest.getData().getTenantId());
        }
        final CrudStringKeyResponse<TenantDTO, FullTrackingWithVersion> result = super.execute(ctx, mapper, resolver, crudRequest);
        if (crudRequest.getCrud() != OperationType.READ) {
            final String tenantId = result.getData() != null ? result.getData().getTenantId() : null;
            cacheInvalidator.invalidateAuthCache(ctx, TenantDTO.class.getSimpleName(), null, tenantId);
        }
        return result;
    }

    private void checkForAllowedTenantId(final String id) {
        if (T9tConstants.GLOBAL_TENANT_ID.equals(id)) {
            return;  // ID is @ exception
        }
        if (ALLOWED_TENANT_ID_PATTERN.matcher(id).matches()) {
            return;
        }
        LOGGER.error("Attempted to create / update to invalid tenant ID pattern {}", id);
        throw new T9tException(T9tAuthException.INVALID_TENANT_ID);
    }
}
