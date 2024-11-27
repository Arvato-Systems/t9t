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
package com.arvatosystems.t9t.out.be.async;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
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

    private record ChannelCacheKey(String tenantId, String channelId) {
    }

    @Override
    public AsyncChannelDTO getCachedAsyncChannelDTO(final String tenantId, final String channelId) {
        return CHANNEL_CACHE.get(new ChannelCacheKey(tenantId, channelId), key -> {
            LOGGER.debug("ASYNC: Updating cache for channel {}", key);
            return messageUpdater.readChannelConfig(key.channelId, key.tenantId);
        });
    }

    @Override
    public boolean tryToSend(final IAsyncSender sender, final InMemoryMessage nextMsg, final int defaultTimeout) {
        // Returns true is the message was sent successfully, else false
        final String channelId = nextMsg.getAsyncChannelId();
        final String tenantId = nextMsg.getTenantId();
        LOGGER.debug("ASYNC: Send message from channel {} of type {}, objectRef {}",
          nextMsg.getAsyncChannelId(), nextMsg.getPayload().ret$PQON(), nextMsg.getObjectRef());
        // obtain (cached) channel config
        final AsyncChannelDTO channel = getCachedAsyncChannelDTO(tenantId, channelId);
        if (!channel.getIsActive()) {
            LOGGER.debug("ASYNC: Discard message due to inactive channel {}", channelId);
            messageUpdater.updateMessage(nextMsg.getObjectRef(), ExportStatusEnum.RESPONSE_OK, null, null);
            return true;
        } else {
            // log message if desired (expensive!)
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("ASYNC: Sending payload {}, objectRef {}", ToStringHelper.toStringML(nextMsg.getPayload()), nextMsg.getObjectRef());
            }

            try {
                final int timeout = channel.getTimeoutInMs() == null ? defaultTimeout : channel.getTimeoutInMs().intValue();
                final Long messageObjectRef = nextMsg.getObjectRef();
                final long whenStarted = System.currentTimeMillis();

                final Consumer<AsyncHttpResponse> resultProcessor = asyncResponse -> {
                    final long whenDone = System.currentTimeMillis();
                    final Integer responseTime = Integer.valueOf((int)(whenDone - whenStarted));
                    asyncResponse.setResponseTime(responseTime);
                    final ExportStatusEnum newStatus2 = sender.httpCodeToStatus(asyncResponse.getHttpReturnCode(), channel);
                    LOGGER.debug("ASYNC: Received response for channel {} ref {}: HTTP code {} mapped to new status {}",
                        channel.getAsyncChannelId(), messageObjectRef, newStatus2, asyncResponse.getHttpReturnCode());
                    messageUpdater.updateMessage(messageObjectRef, newStatus2, asyncResponse.getHttpReturnCode(), asyncResponse);
                };
                return sender.send(channel, timeout, nextMsg, resultProcessor, whenStarted);
            } catch (SocketException | UnknownHostException | SocketTimeoutException e) {
                LOGGER.error(
                        "ASYNC: IO Exception in outbound HTTP call for channel {}, message with ref {}: {}. "
                        + "Allow retry of async messages as later processing is likely to succeed.",
                        channel.getAsyncChannelId(), nextMsg.getObjectRef(), ExceptionUtil.causeChain(e));
                messageUpdater.updateMessage(nextMsg.getObjectRef(), ExportStatusEnum.RESPONSE_TIMEOUT, T9tConstants.HTTP_STATUS_INTERNAL_TIMEOUT, null);
                return false;   // error, gate to RED
            } catch (final Exception e) {
                LOGGER.error(
                        "ASYNC: Exception in outbound HTTP call for channel {}, message with ref {}: {}. RESPONSE_ABORT will be set as a final state. "
                        + "No retry will be performed.",
                        channel.getAsyncChannelId(), nextMsg.getObjectRef(), ExceptionUtil.causeChain(e));
                messageUpdater.updateMessage(nextMsg.getObjectRef(), ExportStatusEnum.RESPONSE_ABORT, T9tConstants.HTTP_STATUS_ILE_OTHER_EXCEPTION, null);
                return false;   // error, gate to RED
            }
        }
    }
}
