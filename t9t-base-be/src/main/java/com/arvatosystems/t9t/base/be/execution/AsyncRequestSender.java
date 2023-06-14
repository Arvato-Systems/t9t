/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.execution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.auth.JwtAuthentication;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.XTargetChannelType;
import com.arvatosystems.t9t.server.services.IAsyncRequestSender;
import com.arvatosystems.t9t.server.services.IEvent;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaparteJsonEscaper;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.bonaparte.core.StringBuilderComposer;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

//send a request to an external arbitrary address (not the vert.x eventBus)
@Singleton
public class AsyncRequestSender implements IAsyncRequestSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRequestSender.class);

    protected final IEvent event = Jdp.getRequired(IEvent.class);
    protected final Provider<RequestContext> ctx = Jdp.getProvider(RequestContext.class);


    @Override
    public void asyncRequest(final XTargetChannelType channel, final String address, final BonaPortable rq, MediaXType serializationFormat) {
        if (serializationFormat == null) {
            serializationFormat = MediaTypes.MEDIA_XTYPE_JSON;
        }
        final MediaData data = new MediaData();
        data.setMediaType(serializationFormat);
        switch ((MediaType) serializationFormat.getBaseEnum()) {
        case JSON:
            data.setText(BonaparteJsonEscaper.asJson(rq));
            break;
        case BONAPARTE:
            data.setText(StringBuilderComposer.marshal(StaticMeta.OUTER_BONAPORTABLE, rq));
            break;
        case COMPACT_BONAPARTE:
            data.setRawData(CompactByteArrayComposer.marshalAsByteArray(StaticMeta.OUTER_BONAPORTABLE, rq));
            break;
        default:
            throw new UnsupportedOperationException("Unsupported media type for IAsyncRequestSender");
        }
        event.asyncEvent(channel, address, data);
        return;
    }

    @Override
    public void asyncServiceRequest(final XTargetChannelType channel, final String address, final RequestParameters rq, final MediaXType serializationFormat) {
        final RequestContext currentContext = ctx.get();
        LOGGER.debug("AsyncServiceRequestSender called for channel {}, address {}, request type {}, format {}, user {}, tenant {}",
            channel.getToken(), address, rq.ret$PQON(),
            serializationFormat == null ? "(default)" : serializationFormat,
            currentContext.userId, currentContext.tenantId);
        ServiceRequestHeader srh = null;
        if (currentContext.internalHeaderParameters.getMessageId() != null) {
            srh = new ServiceRequestHeader();
            srh.setMessageId(currentContext.internalHeaderParameters.getMessageId());
            srh.setIdempotencyBehaviour(currentContext.internalHeaderParameters.getIdempotencyBehaviour());
        }
        final ServiceRequest srq = new ServiceRequest();
        srq.setRequestHeader(srh);
        srq.setRequestParameters(rq);
        srq.setAuthentication(new JwtAuthentication(currentContext.internalHeaderParameters.getEncodedJwt()));
        asyncRequest(channel, address, srq, serializationFormat);
    }
}
