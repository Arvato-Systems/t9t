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
package com.arvatosystems.t9t.doc.jpa.request;

import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocConfigDTO;
import com.arvatosystems.t9t.doc.DocConfigRef;
import com.arvatosystems.t9t.doc.jpa.entities.DocConfigEntity;
import com.arvatosystems.t9t.doc.jpa.mapping.IDocConfigDTOMapper;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocConfigEntityResolver;
import com.arvatosystems.t9t.doc.request.DocConfigCrudRequest;

import de.jpaw.dp.Jdp;

public class DocModuleCfgCrudRequestHandler
  extends AbstractCrudSurrogateKeyRequestHandler<DocConfigRef, DocConfigDTO, FullTrackingWithVersion, DocConfigCrudRequest, DocConfigEntity> {
    private final IDocConfigEntityResolver resolver = Jdp.getRequired(IDocConfigEntityResolver.class);
    private final IDocConfigDTOMapper mapper = Jdp.getRequired(IDocConfigDTOMapper.class);

    @Override
    public CrudSurrogateKeyResponse<DocConfigDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final DocConfigCrudRequest request)
        throws Exception {

        return execute(ctx, mapper, resolver, request);
    }
}
