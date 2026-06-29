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
package com.arvatosystems.t9t.out.be.async;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.cfg.be.AsyncTransmitterConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.out.services.IAsyncSender;
import com.arvatosystems.t9t.out.services.IAsyncTools;

public abstract class AbstractAsyncQueueData {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsyncQueueData.class);
    private static final int DEFAULT_TIMEOUT_EXTERNAL = 5000;

    protected final AsyncQueueDTO queueConfig;

    protected final AtomicBoolean shutdownInProgress = new AtomicBoolean();
    protected final AtomicReference<Instant> lastMessageSent = new AtomicReference<>();
    protected final AtomicBoolean gate = new AtomicBoolean(true); // true is GREEN, false is RED
    protected final IAsyncTools asyncTools = Jdp.getRequired(IAsyncTools.class);
    protected final IAsyncSender sender;
    protected final AsyncTransmitterConfiguration asyncTransmitterConfig = ConfigProvider.getConfiguration().getAsyncMsgConfiguration();

    protected AbstractAsyncQueueData(@Nonnull final AsyncQueueDTO queueConfig) {
        this.queueConfig = queueConfig;
        sender = Jdp.getRequired(IAsyncSender.class, queueConfig.getSenderQualifier() == null ? "POST" : queueConfig.getSenderQualifier());
        sender.init(queueConfig);
    }

    @Nonnull
    public AsyncQueueDTO getQueueConfig() {
        return queueConfig;
    }

    protected boolean sendAsync(@Nullable final BonaPortable payload) {
        if (payload instanceof InMemoryMessage message) {
            LOGGER.debug("Delivering received message from queue {}, channel {}, objectRef {}, payload type {}", queueConfig.getAsyncQueueId(), message.getAsyncChannelId(), message.getObjectRef(),
                    message.getPayload().getClass().getCanonicalName());
            if (asyncTools.tryToSend(sender, message, queueConfig.getTimeoutExternal() == null ? DEFAULT_TIMEOUT_EXTERNAL : queueConfig.getTimeoutExternal())) {
                lastMessageSent.set(Instant.now());
                return true;
            } else {
                LOGGER.debug("Failed to send message from queue {}, channel {}, objectRef {}, payload type {}", queueConfig.getAsyncQueueId(), message.getAsyncChannelId(), message.getObjectRef(),
                        message.getPayload().getClass().getCanonicalName());
            }
        } else {
            LOGGER.error("Received data from queue {} is not the expected type {}, but {}", queueConfig.getAsyncQueueId(),
                    InMemoryMessage.class.getCanonicalName(), payload == null ? "NULL" : payload.getClass().getCanonicalName());
        }
        return false;
    }
    protected void shutdown() {
        LOGGER.debug("Shutting down queue {}", queueConfig.getAsyncQueueId());
        shutdownInProgress.set(true);
        if (sender != null) {
            sender.close();
        }
    }

    protected abstract void clearQueue();

    public boolean isShutdownInProgress() {
        return shutdownInProgress.get();
    }

    public boolean isGateGreen() {
        return gate.get();
    }

    @Nonnull
    public Instant getLastMessageSent() {
        return lastMessageSent.get();
    }

    /**
     * Split the thread sleep in order to react to config changes while in error state
     * Wake up every 3 seconds at latest
     *
     * @param wait
     * @throws InterruptedException
     */
    protected void sleepShallow(int wait) throws InterruptedException {
        int slept = 0;
        int shallowSleep = Math.min(wait, 3000);
        LOGGER.debug("Start shallow sleep for queue {}", queueConfig.getAsyncQueueId());
        while (slept < wait && !shutdownInProgress.get()) {
            Thread.sleep(shallowSleep);
            slept += shallowSleep;
        }
        LOGGER.debug("Stop shallow sleep for queue {}", queueConfig.getAsyncQueueId());
    }
}
