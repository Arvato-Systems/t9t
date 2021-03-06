/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.jpa.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.AsyncTransmitterConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.io.jpa.entities.AsyncMessageEntity;
import com.arvatosystems.t9t.io.request.QueueStatus;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;
import com.arvatosystems.t9t.out.services.IAsyncQueue;
import com.arvatosystems.t9t.out.services.IAsyncSender;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
@Named("LTQ")
public class AsyncQueueLTQ<R extends BonaPortable> implements IAsyncQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueueLTQ.class);
    private static final Cache<ChannelCacheKey, AsyncChannelDTO> channelCache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();

    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);
    private final IAsyncMessageUpdater messageUpdater = Jdp.getRequired(IAsyncMessageUpdater.class);
    private final ConcurrentMap<Long, QueueData> queueData;

    public static class ChannelCacheKey {
        public final Long   tenantRef;
        public final String channelId;
        public ChannelCacheKey(Long tenantRef, String channelId) {
            this.tenantRef = tenantRef;
            this.channelId = channelId;
        }
    }

    /** Keeps the queue configuration and the references to their threads. */
    private static class QueueData {
        private final WriterThread    writerThread;
        private final ExecutorService executor;

        private QueueData(AsyncQueueDTO queueConfig) {
            queueConfig.freeze();
            executor = Executors.newSingleThreadExecutor(t -> new Thread(t, "t9t-AsyncTx-" + queueConfig.getAsyncQueueId()));
            this.writerThread = new WriterThread(queueConfig);
            executor.submit(writerThread);
        }

        private void shutdown(int timeout) {
            writerThread.close();
            writerThread.shutdownInProgress.set(true);
            executor.shutdown();
            if (timeout < 1000)
                timeout = 1000;
            try {
                if (executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                    LOGGER.info("Normal completion of shutting down async transmitter");
                } else {
                    LOGGER.warn("Timeout during shutdown of async transmitter");
                }
            } catch (InterruptedException e) {
                LOGGER.warn("Shutdown of async transmitter was interrupted");
            }
        }
    }

    public AsyncQueueLTQ() {
        LOGGER.info("Async queue by LinkedTransferQueue loaded");

        // read the configured queues and launch a thread for each of them...
        final List<AsyncQueueDTO> queueDTOs = messageUpdater.getActiveQueues();
        queueData = new ConcurrentHashMap<>(3 * queueDTOs.size());
        if (queueDTOs.size() > 0) {
            // do not use fixed size pool, because it will create an initial thread before the writers are submitted,
            // which means we do not yet have access to their name.
            for (AsyncQueueDTO q: queueDTOs) {
                try {
                    queueData.put(q.getObjectRef(), new QueueData(q));
                } catch (Exception e) {
                    LOGGER.error("Cannot launch async writer thread for queue {} due to {}", q.getAsyncQueueId(), ExceptionUtil.causeChain(e));
                }
            }
        }
    }

    private static class WriterThread implements Runnable {
        private final LinkedTransferQueue<InMemoryMessage> queue = new LinkedTransferQueue<>();
        private final String threadName;
        private final Boolean lock = new Boolean(true);  // separate object used as semaphore
        private final AtomicBoolean gate = new AtomicBoolean();  // true is GREEN, false is RED
        private final AtomicBoolean shutdownInProgress = new AtomicBoolean();
        private final AsyncTransmitterConfiguration serverConfig;
        private final AsyncQueueDTO myQueueCfg;
        private final IAsyncSender sender;
        private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);
        private final IAsyncMessageUpdater messageUpdater = Jdp.getRequired(IAsyncMessageUpdater.class);
        private final AsyncTransmitterConfiguration globalServerConfig = ConfigProvider.getConfiguration().getAsyncMsgConfiguration();
        private final AtomicReference<Instant> lastMessageSent = new AtomicReference<>();

        private WriterThread(AsyncQueueDTO myCfg) {
            myQueueCfg = myCfg;
            myQueueCfg.freeze();
            threadName = "t9t-AsyncTx-" + myCfg.getAsyncQueueId();
            serverConfig = globalServerConfig.ret$MutableClone(true, false);
            // merge queue config into global config
            if (myCfg.getMaxMessageAtStartup() != null)
                serverConfig.setMaxMessageAtStartup(myCfg.getMaxMessageAtStartup());
            if (myCfg.getTimeoutIdleGreen() != null)
                serverConfig.setTimeoutIdleGreen(myCfg.getTimeoutIdleGreen());
            if (myCfg.getTimeoutIdleRed() != null)
                serverConfig.setTimeoutIdleRed(myCfg.getTimeoutIdleRed());
            if (myCfg.getTimeoutExternal() != null)
                serverConfig.setTimeoutExternal(myCfg.getTimeoutExternal());
            if (myCfg.getWaitAfterExtError() != null)
                serverConfig.setWaitAfterExtError(myCfg.getWaitAfterExtError());
            if (myCfg.getWaitAfterDbErrors() != null)
                serverConfig.setWaitAfterDbErrors(myCfg.getWaitAfterDbErrors());

            sender = Jdp.getRequired(IAsyncSender.class, myCfg.getSenderQualifier() == null ? "POST" : myCfg.getSenderQualifier());
            sender.init(myCfg);
        }

        @Override
        public void run() {
            LOGGER.info("Starting async thread {} for queue {}", threadName, myQueueCfg.getAsyncQueueId());
            while (!shutdownInProgress.get()) {
                try {
                    final InMemoryMessage nextMsg = queue.peek();
                    if (nextMsg != null) {
                        if (!tryToSend(nextMsg)) {
                            // switch to RED and wait
                            if (gate.getAndSet(false))
                                LOGGER.debug("Flipping gate to RED (transmission error)");
                            Thread.sleep(serverConfig.getWaitAfterExtError());
                        } else {
                            // eat message, it was sent successfully
                            lastMessageSent.set(new Instant());
                            if (queue.poll() == null) {
                                LOGGER.error("ILE: queue element no longer available!");
                            }
                        }
                    } else if (gate.get()) {
                        // gate is "GREEN", any message would be in memory, if it existed. No need to check the DB
                        Thread.sleep(serverConfig.getTimeoutIdleGreen());
                    } else {
                        // gate is "RED"
                        // no message in the queue now, refill queue from DB
                        if (!refillQueue()) {
                            // we are really idle and have switched to "GREEN" during the call to refillQueue(). Must wait the same time as above
                            Thread.sleep(serverConfig.getTimeoutIdleGreen());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception in Async transmitter thread: {}", ExceptionUtil.causeChain(e));
                    LOGGER.error("Trace is", e);
                    try {
                        Thread.sleep(serverConfig.getWaitAfterExtError());
                    } catch (InterruptedException e1) {
                        LOGGER.error("Sleep disturbed, terminating!");
                        break;  // terminate thread!
                    }
                }
            }
            LOGGER.info("Stopping async thread {} for queue {} due to shutdown request: closing sender", threadName, myQueueCfg.getAsyncQueueId());
            sender.close();
            LOGGER.info("Stopping async thread {} for queue {} due to shutdown request: finished", threadName, myQueueCfg.getAsyncQueueId());
        }

        // fills the queue from the DB. Returns true if there was at least one element (or an exception has occurred), else false.
        // the gate is "RED" if we enter here. Synchronization is needed if we want to flip to "GREEN", in order to avoid race conditions
        protected boolean refillQueue() {
            final EntityManager em = emf.createEntityManager();
            List<AsyncMessageEntity> results = null;

            synchronized(lock) {
                try {
                    em.getTransaction().begin();
                    TypedQuery<AsyncMessageEntity> query = em.createQuery(
                            "SELECT m FROM AsyncMessageEntity m WHERE m.status != null AND m.asyncQueueRef = :queueRef ORDER BY m.objectRef",
                            AsyncMessageEntity.class);
                    query.setParameter("queueRef", myQueueCfg.getObjectRef());
                    query.setMaxResults(serverConfig.getMaxMessageAtStartup());
                    results = query.getResultList();
                    em.getTransaction().commit();
                    em.clear();
                } catch (Exception e) {
                    LOGGER.error("Database query exception: {}", ExceptionUtil.causeChain(e));
                    try {
                        Thread.sleep(serverConfig.getWaitAfterDbErrors());
                    } catch (InterruptedException e1) {
                        // continue with aborted sleep
                        return true;
                    }
                } finally {
                    em.close();
                }
                if (results == null) {
                    // error: do nothing
                    return false;
                } else if (results.size() == 0) {
                    // switch to green, no data pending
                    LOGGER.debug("Flipping gate to GREEN (queue empty)");
                    gate.set(true);
                    return false;
                }
                // add them to the queue
                for (AsyncMessageEntity m : results) {
                    queue.put(new InMemoryMessage(m.getTenantRef(), m.getAsyncChannelId(), m.getObjectRef(), m.getPayload()));
                }
                if (results.size() < serverConfig.getMaxMessageAtStartup()) {
                    LOGGER.debug("Flipping gate to GREEN (low watermark)");
                    gate.set(true);
                }
            }
            return true;
        }

        private void send(RequestContext ctx, InMemoryMessage m) {
            synchronized(lock) {
                if (gate.get()) {
                    // we are in "GREEN" status
                    ctx.addPostCommitHook((RequestContext oldCtx, RequestParameters rq, ServiceResponse rs) -> {
                        queue.put(m);
                    });
                }
            }
        }

        private void clearQueue() {
            // drain the queue! This is done after artificially removing entries from the queue
            queue.clear();
            gate.set(true);  // gate must be true now, because we otherwise will never poll again
        }

        private void close() {
            LOGGER.info("Shutting down async transmitter {} (in current state {})", threadName, gate.get());
            gate.set(false);
        }

        // sends a message. In case of error, the gate is flipped to "RED" and a long timeout is done, in order to ensure we do not burn CPU
        // in high frequency retry cycles.
        // Returns true is the message was sent successfully, else false
        protected boolean tryToSend(final InMemoryMessage nextMsg) {
            ExportStatusEnum newStatus = ExportStatusEnum.RESPONSE_ERROR;  // OK when sent
            final String channelId = nextMsg.getAsyncChannelId();
            final Long tenantRef = nextMsg.getTenantRef();
            try {
                LOGGER.info("Sending message to channel {} of type {}", nextMsg.getAsyncChannelId(), nextMsg.getPayload().ret$PQON());
                // obtain (cached) channel config
                final AsyncChannelDTO channelDto = channelCache.get(new ChannelCacheKey(tenantRef, channelId), () -> messageUpdater.readChannelConfig(channelId, tenantRef));
                if (!channelDto.getIsActive()) {
                    LOGGER.debug("Discarding async message to inactive channel {}", channelId);
                    newStatus = ExportStatusEnum.RESPONSE_OK;
                } else {

                    // log message if desired (expensive!)
                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Sending payload {}, objectRef {}", ToStringHelper.toStringML(nextMsg.getPayload()), nextMsg.getObjectRef());
                    }

                    Integer newHttpCode = null;
                    Integer newClientReturnCode = null;
                    String newClientReference = null;
                    try {
                        int timeout = channelDto.getTimeoutInMs() == null ? serverConfig.getTimeoutExternal() : channelDto.getTimeoutInMs().intValue();
                        AsyncHttpResponse resp =  sender.send(channelDto, nextMsg.getPayload(), timeout, nextMsg.getObjectRef());
                        newHttpCode = resp.getHttpReturnCode();
                        newStatus = (newHttpCode / 100) == 2 ? ExportStatusEnum.RESPONSE_OK : ExportStatusEnum.RESPONSE_ERROR;
                        newClientReturnCode = resp.getClientReturnCode();
                        newClientReference = resp.getClientReference();
                    } catch (Exception e) {
                        LOGGER.error("Exception in external http: {}", ExceptionUtil.causeChain(e));
                        newHttpCode = 999;
                    }
                    messageUpdater.updateMessage(nextMsg.getObjectRef(), newStatus, newHttpCode, newClientReturnCode, newClientReference);
                    return newStatus == ExportStatusEnum.RESPONSE_OK;
                }
            } catch (ExecutionException e) {
                LOGGER.error("Cannot get cache configuration for channelId {}: {}", channelId, ExceptionUtil.causeChain(e));
                newStatus = ExportStatusEnum.PROCESSING_ERROR;
            }

            messageUpdater.updateMessage(nextMsg.getObjectRef(), newStatus, null, null, null);
            return newStatus == ExportStatusEnum.RESPONSE_OK;
        }
    }

    @Override
    public Long sendAsync(final String asyncChannelId, BonaPortable payload, Long objectRef) {
        final RequestContext ctx = ctxProvider.get();
        // redundant check to see if the channel exists (to get exception in sync thread already). Should not cost too much time due to caching
        try {
            final AsyncChannelDTO cfg = channelCache.get(new ChannelCacheKey(ctx.tenantRef, asyncChannelId), () -> messageUpdater.readChannelConfig(asyncChannelId, ctx.tenantRef));
            if (!cfg.getIsActive() || cfg.getAsyncQueueRef() == null) {
                LOGGER.debug("Discarding async message to inactive or unassociated channel {}", asyncChannelId);
                return null;
            }
            final Long asyncQueueRef = cfg.getAsyncQueueRef().getObjectRef();
            final QueueData queue = queueData.get(asyncQueueRef);

            if (queue != null) {
                // queue is currently active: build the in-memory message and transmit it
                final InMemoryMessage m = new InMemoryMessage();
                m.setTenantRef(ctx.tenantRef);       // obtain the tenantRef and store it
                m.setAsyncChannelId(asyncChannelId);
                m.setObjectRef(objectRef);
                m.setPayload(payload);
                queue.writerThread.send(ctx, m);
            }
            return asyncQueueRef;
        } catch (ExecutionException e) {
            LOGGER.error("Cannot get cache configuration for channelId {}: {}", asyncChannelId, ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "ChannelId " + asyncChannelId);
        }
    }

    protected void shutdown(QueueData w) {
        w.shutdown(ConfigProvider.getConfiguration().getAsyncMsgConfiguration().getTimeoutShutdown());
    }

    @Override
    public void close() {
        for (QueueData w: queueData.values()) {
            shutdown(w);
        }
        queueData.clear();
    }

    @Override
    public void close(Long queueRef) {
        QueueData w = queueData.get(queueRef);
        if (w != null) {
            shutdown(w);
            queueData.remove(queueRef);
        } else {
            LOGGER.error("Cannot find queue data for {}", queueRef);
        }
    }

    @Override
    public void clearQueue(Long queueRef) {
        if (queueRef == null) {
            for (QueueData w: queueData.values()) {
                w.writerThread.clearQueue();
            }
        } else {
            QueueData w = queueData.get(queueRef);
            if (w != null) {
                w.writerThread.clearQueue();
            } else {
                LOGGER.error("Cannot find queue data for {}", queueRef);
            }
        }
    }

    @Override
    public void open(AsyncQueueDTO queue) {
        queueData.computeIfAbsent(queue.getObjectRef(), (x) -> new QueueData(queue));
    }

    @Override
    public QueueStatus getQueueStatus(Long queueRef, String queueId) {
        final QueueData w = queueData.get(queueRef);
        final QueueStatus status = new QueueStatus();
        status.setAsyncQueueId(queueId);
        if (w == null) {
            status.setRunning(false);
        } else {
            final WriterThread wt = w.writerThread;
            status.setRunning(true);
            status.setIsGreen(wt.gate.get());
            status.setLastMessageSent(wt.lastMessageSent.get());
            status.setShuttingDown(wt.shutdownInProgress.get());
        }
        return status;
    }
}
