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
package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.TenantLogoDTO;
import com.arvatosystems.t9t.auth.jpa.entities.TenantLogoEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.ITenantLogoDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantLogoEntityResolver;
import com.arvatosystems.t9t.auth.request.TenantLogoCrudRequest;
import com.arvatosystems.t9t.base.IUploadChecker;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudModuleCfgRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class TenantLogoCrudRequestHandler extends AbstractCrudModuleCfgRequestHandler<TenantLogoDTO, TenantLogoCrudRequest, TenantLogoEntity> {

    private final ITenantLogoDTOMapper mapper = Jdp.getRequired(ITenantLogoDTOMapper.class);

    private final ITenantLogoEntityResolver resolver = Jdp.getRequired(ITenantLogoEntityResolver.class);
    private final IUploadChecker uploadChecker = Jdp.getRequired(IUploadChecker.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final TenantLogoCrudRequest params) {
        if (params.getData() != null) {
            uploadChecker.virusCheck(params.getData().getLogo());
        }
        return execute(ctx, mapper, resolver, params);
    }
}
