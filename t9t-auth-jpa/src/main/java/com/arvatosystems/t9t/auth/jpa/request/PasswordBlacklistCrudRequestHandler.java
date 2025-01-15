/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.util.Locale;

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.PasswordBlacklistDTO;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordBlacklistEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IPasswordBlacklistDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IPasswordBlacklistEntityResolver;
import com.arvatosystems.t9t.auth.request.PasswordBlacklistCrudRequest;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudStringKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;

public class PasswordBlacklistCrudRequestHandler
        extends AbstractCrudStringKeyRequestHandler<PasswordBlacklistDTO, NoTracking, PasswordBlacklistCrudRequest, PasswordBlacklistEntity> {

    private final IAuthModuleCfgDtoResolver authModuleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);
    private final IPasswordBlacklistEntityResolver resolver = Jdp.getRequired(IPasswordBlacklistEntityResolver.class);
    private final IPasswordBlacklistDTOMapper mapper = Jdp.getRequired(IPasswordBlacklistDTOMapper.class);

    @Override
    public CrudStringKeyResponse<PasswordBlacklistDTO, NoTracking> execute(final RequestContext ctx, PasswordBlacklistCrudRequest crudRequest) {

        final AuthModuleCfgDTO authModuleCfg = authModuleCfgResolver.getModuleConfiguration() == null ? IAuthPersistenceAccess.DEFAULT_MODULE_CFG
                : authModuleCfgResolver.getModuleConfiguration();

        if (Boolean.TRUE.equals(authModuleCfg.getPasswordCheckCaseInsensitive())) {
            OperationType currentOperation = crudRequest.getCrud();
            if (OperationType.CREATE.equals(currentOperation) || OperationType.MERGE.equals(currentOperation)) {
                // in these cases (for this configuration) now always convert password to UPPER case (to optimize runtime in searches later)
                final PasswordBlacklistDTO currentDTO = crudRequest.getData();
                final String convertedPassword = currentDTO.getPasswordInBlacklist().toUpperCase(Locale.getDefault());
                currentDTO.setPasswordInBlacklist(convertedPassword);
            }
        }

        return super.execute(ctx, mapper, resolver, crudRequest);
    }
}
