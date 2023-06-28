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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncMessageRef;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncMessageEntityResolver;
import com.arvatosystems.t9t.io.request.UpdateAsyncMessageStatusRequest;
import com.arvatosystems.t9t.out.services.IAsyncTransmitter;

import de.jpaw.dp.Jdp;

public class UpdateAsyncMessageStatusRequestHandler extends AbstractRequestHandler<UpdateAsyncMessageStatusRequest> {
    private final IAsyncMessageEntityResolver messageResolver   = Jdp.getRequired(IAsyncMessageEntityResolver.class);
    private final IAsyncTransmitter           asyncTransmitter  = Jdp.getRequired(IAsyncTransmitter.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final UpdateAsyncMessageStatusRequest rq) {
        final AsyncMessageEntity message = messageResolver.getEntityData(new AsyncMessageRef(rq.getAsyncMessageRef()), false);
        final boolean shouldResend = rq.getNewStatus() == ExportStatusEnum.READY_TO_EXPORT;
        message.setStatus(rq.getNewStatus());
        // message.setLastAttempt(ctx.executionStart);  // do not update lastAttempt before it actually has been attempted
        message.setHttpResponseCode(rq.getNewStatus() == ExportStatusEnum.RESPONSE_ERROR ? Integer.valueOf(500) : Integer.valueOf(shouldResend ? 100 : 202));
        message.setReturnCode(null);
        message.setReference(null);
        if (shouldResend) {
            // initiate a resend, if this implementation requires it (unfortunately we do not know the partition any more)
            asyncTransmitter.retransmitMessage(ctx, message.getAsyncChannelId(), message.getPayload(), message.getObjectRef(), 0, null);
        }
        return ok();
    }
}
