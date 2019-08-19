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
package com.arvatosystems.t9t.doc.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocEmailCfgDTO;
import com.arvatosystems.t9t.doc.DocEmailCfgRef;
import com.arvatosystems.t9t.doc.request.DocEmailCfgCrudRequest;
import com.arvatosystems.t9t.doc.services.IDocEmailCfgResolver;

import de.jpaw.dp.Jdp;

public class DocEmailCfgCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<DocEmailCfgRef, DocEmailCfgDTO, FullTrackingWithVersion, DocEmailCfgCrudRequest> {

    protected final IDocEmailCfgResolver resolver = Jdp.getRequired(IDocEmailCfgResolver.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, DocEmailCfgCrudRequest crudRequest) throws Exception {
        return execute(ctx, crudRequest, resolver);
    }
}
