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
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.DocComponentRef;
import com.arvatosystems.t9t.doc.be.impl.DocFormatter;
import com.arvatosystems.t9t.doc.request.DocComponentCrudRequest;
import com.arvatosystems.t9t.doc.services.IDocComponentResolver;

import de.jpaw.dp.Jdp;

public class DocComponentCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<DocComponentRef, DocComponentDTO, FullTrackingWithVersion, DocComponentCrudRequest> {

    protected final IDocComponentResolver resolver = Jdp.getRequired(IDocComponentResolver.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, DocComponentCrudRequest crudRequest) throws Exception {
        DocFormatter.clearCache();   // this could change the configuration, clear the cache immediately!
        executor.clearCache(DocComponentDTO.class.getSimpleName(), null);
        return execute(ctx, crudRequest, resolver);
    }
}
