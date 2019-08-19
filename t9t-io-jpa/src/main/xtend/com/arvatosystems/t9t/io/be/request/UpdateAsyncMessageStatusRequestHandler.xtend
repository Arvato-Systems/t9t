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
package com.arvatosystems.t9t.io.be.request

import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.AsyncMessageRef
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver
import com.arvatosystems.t9t.io.request.UpdateAsyncMessageStatusRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

@AddLogger
class UpdateAsyncMessageStatusRequestHandler extends AbstractRequestHandler<UpdateAsyncMessageStatusRequest> {
    @Inject IAsyncMessageEntityResolver messageResolver

    override execute(RequestContext ctx, UpdateAsyncMessageStatusRequest rq) {
        val message = messageResolver.getEntityData(new AsyncMessageRef(rq.asyncMessageRef), false)
        message.status           = rq.newStatus
        message.lastAttempt      = ctx.executionStart
        message.httpResponseCode = 911
        message.returnCode       = null
        message.reference        = null
        return ok
    }
}
