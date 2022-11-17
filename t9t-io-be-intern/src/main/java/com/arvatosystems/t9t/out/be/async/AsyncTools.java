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
package com.arvatosystems.t9t.out.be.async;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;
import com.arvatosystems.t9t.out.services.IAsyncSender;
import com.arvatosystems.t9t.out.services.IAsyncTools;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class AsyncTools implements IAsyncTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncTools.class);
    private static final Cache<ChannelCacheKey, AsyncChannelDTO> CHANNEL_CACHE = Caffeine.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();

    private final IAsyncMessageUpdater messageUpdater = Jdp.getRequired(IAsyncMessageUpdater.class);

    public static class ChannelCacheKey {
        public final String  tenantId;
        public final String channelId;
        public ChannelCacheKey(final String tenantId, final String channelId) {
            this.tenantId = tenantId;
            this.channelId = channelId;
        }
    }

    @Override
    public AsyncChannelDTO getCachedAsyncChannelDTO(final String tenantId, final String channelId) {
        return CHANNEL_CACHE.get(new ChannelCacheKey(tenantId, channelId),
                  unused -> messageUpdater.readChannelConfig(channelId, tenantId));
    }

    @Override
    public boolean tryToSend(final IAsyncSender sender, final InMemoryMessage nextMsg, final int defaultTimeout) {
        // Returns true is the message was sent successfully, else false
        ExportStatusEnum newStatus = ExportStatusEnum.RESPONSE_ERROR;  // OK when sent
        final String channelId = nextMsg.getAsyncChannelId();
        final String tenantId = nextMsg.getTenantId();
        LOGGER.debug("Sending message to channel {} of type {}", nextMsg.getAsyncChannelId(), nextMsg.getPayload().ret$PQON());
        // obtain (cached) channel config
        final AsyncChannelDTO channelDto = getCachedAsyncChannelDTO(tenantId, channelId);
        if (!channelDto.getIsActive()) {
            LOGGER.debug("Discarding async message to inactive channel {}", channelId);
            messageUpdater.updateMessage(nextMsg.getObjectRef(), ExportStatusEnum.RESPONSE_OK, null, null, null, null);
            return true;
        } else {
            // log message if desired (expensive!)
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Sending payload {}, objectRef {}", ToStringHelper.toStringML(nextMsg.getPayload()), nextMsg.getObjectRef());
            }

            Integer newHttpCode = null;
            Integer newClientReturnCode = null;
            String newClientReference = null;
            String newClientErrorDetails = null;
            try {
                final int timeout = channelDto.getTimeoutInMs() == null ? defaultTimeout : channelDto.getTimeoutInMs().intValue();
                final AsyncHttpResponse resp =  sender.send(channelDto, nextMsg.getPayload(), timeout, nextMsg.getObjectRef());
                newHttpCode = resp.getHttpReturnCode();
                newStatus = (newHttpCode / 100) == 2 ? ExportStatusEnum.RESPONSE_OK : ExportStatusEnum.RESPONSE_ERROR;
                newClientReturnCode = resp.getClientReturnCode();
                newClientReference = resp.getClientReference();
                newClientErrorDetails = resp.getErrorDetails();
            } catch (final Exception e) {
                LOGGER.error("Exception in external http: {}", ExceptionUtil.causeChain(e));
                newHttpCode = 999;
            }
            messageUpdater.updateMessage(nextMsg.getObjectRef(), newStatus, newHttpCode, newClientReturnCode, newClientReference, newClientErrorDetails);
            return newStatus == ExportStatusEnum.RESPONSE_OK;
        }
    }
}
