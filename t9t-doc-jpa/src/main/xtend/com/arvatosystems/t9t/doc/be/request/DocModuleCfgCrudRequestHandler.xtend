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
package com.arvatosystems.t9t.doc.be.request

import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudModuleCfg42RequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.DocModuleCfgDTO
import com.arvatosystems.t9t.doc.jpa.entities.DocModuleCfgEntity
import com.arvatosystems.t9t.doc.jpa.mapping.IDocModuleCfgDTOMapper
import com.arvatosystems.t9t.doc.jpa.persistence.IDocModuleCfgEntityResolver
import com.arvatosystems.t9t.doc.request.DocModuleCfgCrudRequest
import de.jpaw.dp.Inject

class DocModuleCfgCrudRequestHandler extends AbstractCrudModuleCfg42RequestHandler<DocModuleCfgDTO, DocModuleCfgCrudRequest, DocModuleCfgEntity> {

    @Inject IDocModuleCfgEntityResolver resolver
    @Inject IDocModuleCfgDTOMapper mapper

    override ServiceResponse execute(RequestContext ctx, DocModuleCfgCrudRequest params) {
        return execute(ctx, mapper, resolver, params);
    }
}
