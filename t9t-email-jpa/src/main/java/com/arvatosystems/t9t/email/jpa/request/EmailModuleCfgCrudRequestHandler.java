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
package com.arvatosystems.t9t.email.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudModuleCfgRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.jpa.entities.EmailModuleCfgEntity;
import com.arvatosystems.t9t.email.jpa.mapping.IEmailModuleCfgDTOMapper;
import com.arvatosystems.t9t.email.jpa.persistence.IEmailModuleCfgEntityResolver;
import com.arvatosystems.t9t.email.request.EmailModuleCfgCrudRequest;
import de.jpaw.dp.Jdp;

public class EmailModuleCfgCrudRequestHandler extends
    AbstractCrudModuleCfgRequestHandler<EmailModuleCfgDTO, EmailModuleCfgCrudRequest, EmailModuleCfgEntity> {

    private final IEmailModuleCfgEntityResolver resolver = Jdp.getRequired(IEmailModuleCfgEntityResolver.class);
    private final IEmailModuleCfgDTOMapper mapper = Jdp.getRequired(IEmailModuleCfgDTOMapper.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, EmailModuleCfgCrudRequest params) {
        return execute(ctx, mapper, resolver, params);
    }
}
