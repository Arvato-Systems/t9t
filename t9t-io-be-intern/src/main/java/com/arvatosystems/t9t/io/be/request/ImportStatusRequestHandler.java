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
package com.arvatosystems.t9t.io.be.request;

import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.request.ImportStatusRequest;
import com.arvatosystems.t9t.io.request.ImportStatusResponse;

public class ImportStatusRequestHandler extends AbstractReadOnlyRequestHandler<ImportStatusRequest> {

    @Override
    public ImportStatusResponse execute(final RequestContext ctx, final ImportStatusRequest rq) {
        final ImportStatusResponse resp = new ImportStatusResponse();
        resp.setReturnCode(0);
        resp.setResponses(rq.getResponses());
        return resp;
    }
}
