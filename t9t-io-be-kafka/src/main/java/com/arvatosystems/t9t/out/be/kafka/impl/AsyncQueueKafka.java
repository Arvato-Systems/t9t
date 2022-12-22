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
package com.arvatosystems.t9t.out.be.kafka.impl;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.AsyncTransmitterConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncQueueDTO;
import com.arvatosystems.t9t.io.InMemoryMessage;
import com.arvatosystems.t9t.io.request.QueueStatus;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.IKafkaTopicWriter;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicReader;
import com.arvatosystems.t9t.kafka.service.impl.KafkaTopicWriter;
import com.arvatosystems.t9t.out.services.IAsyncMessageUpdater;
import com.arvatosystems.t9t.out.services.IAsyncQueue;
import com.arvatosystems.t9t.out.services.IAsyncSender;
import com.arvatosystems.t9t.out.services.IAsyncTools;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
@Named("KAFKA")
public class AsyncQueueKafka<R extends BonaPortable> implements IAsyncQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncQueueKafka.class);

    private final IAsyncMessageUpdater messageUpdater = Jdp.getRequired(IAsyncMessageUpdater.class);
    private final boolean writeAllToDatabase = ConfigProvider.getCustomParameter("NoAsyncPersist") == null;
    private final ConcurrentMap<Long, QueueData> queueData;
    private final IAsyncTools asyncTools = Jdp.getRequired(IAsyncTools.class);

    /** Keeps the queue configuration and the references to their threads. */
    private static final class QueueData {
        private final IKafkaTopicWriter kafkaWriter;
        private final WriterThread    writerThread;
        private final ExecutorService executor;

        private QueueData(final AsyncQueueDTO queueConfig) {
            queueConfig.freeze();

            // obtain the kafka configuration
            final String topic = queueConfig.getKafkaTopic();
            if (topic == null) {
                throw new T9tException(T9tException.MISSING_CONFIGURATION, "need 'kafkaTopic' in AsyncQueueDTO " + queueConfig.getAsyncQueueId());
            }
            final KafkaConfiguration kafkaConfig = ConfigProvider.getConfiguration().getKafkaConfiguration();
            final String defaultBootstrapServers = kafkaConfig == null ? null : kafkaConfig.getDefaultBootstrapServers();
            final String bootstrapServers = T9tUtil.nvl(queueConfig.getKafkaBootstrapServers(), defaultBootstrapServers);
            if (bootstrapServers == null) {
                throw new T9tException(T9tException.MISSING_CONFIGURATION, "need kafka 'bootstrapServers' in AsyncQueueDTO " + queueConfig.getAsyncQueueId());
            }
            kafkaWriter = new KafkaTopicWriter(bootstrapServers, topic, new Properties());

            executor = Executors.newSingleThreadExecutor(t -> new Thread(t, "t9t-AsyncTx-" + queueConfig.getAsyncQueueId()));
            this.writerThread = new WriterThread(queueConfig, bootstrapServers, topic);
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
            } catch (final InterruptedException e) {
                LOGGER.warn("Shutdown of async transmitter was interrupted");
            }
        }
    }

    public AsyncQueueKafka() {
        LOGGER.info("Async queue by KAFKA loaded");

        // read the configured queues and launch a thread for each of them...
        final List<AsyncQueueDTO> queueDTOs = messageUpdater.getActiveQueues();
        queueData = new ConcurrentHashMap<>(3 * queueDTOs.size());
        if (queueDTOs.size() > 0) {
            // do not use fixed size pool, because it will create an initial thread before the writers are submitted,
            // which means we do not yet have access to their name.
            for (final AsyncQueueDTO q: queueDTOs) {
                try {
                    queueData.put(q.getObjectRef(), new QueueData(q));
                } catch (final Exception e) {
                    LOGGER.error("Cannot launch async writer thread for queue {} due to {}", q.getAsyncQueueId(), ExceptionUtil.causeChain(e));
                }
            }
        }
    }

    private static final class WriterThread implements Runnable {
        private final String threadName;
        private final AtomicBoolean gate = new AtomicBoolean();  // true is GREEN, false is RED
        private final AtomicBoolean shutdownInProgress = new AtomicBoolean();
        private final AsyncTransmitterConfiguration serverConfig;
        private final AsyncQueueDTO myQueueCfg;
        private final IAsyncSender sender;
        private final IAsyncTools asyncTools = Jdp.getRequired(IAsyncTools.class);
        private final AsyncTransmitterConfiguration globalServerConfig = ConfigProvider.getConfiguration().getAsyncMsgConfiguration();
        private final AtomicReference<Instant> lastMessageSent = new AtomicReference<>();
        private final IKafkaTopicReader kafkaReader;

        private WriterThread(final AsyncQueueDTO myCfg, final String bootstrapServers, final String topic) {
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

            final Map<String, Object> props = new HashMap<>(16);
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);  // or "false" as found in examples?
            props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60_000);  // 1 minute
            props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 60);  // do not overload the system
            if (myCfg.getZ() != null) {
                final Object extraKafkaConfig = myCfg.getZ().get("kafka");
                if (extraKafkaConfig instanceof Map<?, ?> extraKafkaConfigMap) {
                    LOGGER.info("Found {} additional consumer configuration properties for kafka in AsyncQueueDTO {}",
                      extraKafkaConfigMap.size(), myCfg.getAsyncQueueId());
                    for (final Map.Entry<?, ?> entry: extraKafkaConfigMap.entrySet()) {
                        props.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
            kafkaReader = new KafkaTopicReader(bootstrapServers, topic, "async", props, null);

            sender = Jdp.getRequired(IAsyncSender.class, myCfg.getSenderQualifier() == null ? "POST" : myCfg.getSenderQualifier());
            sender.init(myCfg);
        }

        @Override
        public void run() {
            LOGGER.info("Starting async thread {} for queue {}", threadName, myQueueCfg.getAsyncQueueId());
            while (!shutdownInProgress.get()) {
                try {
                    kafkaReader.pollAndProcess((k,  m) -> asyncTools.tryToSend(sender, m, serverConfig.getTimeoutExternal()), InMemoryMessage.class);
                } catch (final Exception e) {
                    LOGGER.error("Exception in Async transmitter thread: {}", ExceptionUtil.causeChain(e));
                    LOGGER.error("Trace is", e);
                    try {
                        Thread.sleep(serverConfig.getWaitAfterExtError());
                    } catch (final InterruptedException e1) {
                        LOGGER.error("Sleep disturbed, terminating!");
                        break;  // terminate thread!
                    }
                }
            }
            LOGGER.info("Stopping async thread {} for queue {} due to shutdown request: closing sender", threadName, myQueueCfg.getAsyncQueueId());
            sender.close();
            LOGGER.info("Stopping async thread {} for queue {} due to shutdown request: finished", threadName, myQueueCfg.getAsyncQueueId());
        }

        private void clearQueue() {
            // drain the queue! This is done after artificially removing entries from the queue
            // queue.clear();  // FIXME: current no operation!
            gate.set(true);  // gate must be true now, because we otherwise will never poll again
        }

        private void close() {
            LOGGER.info("Shutting down async transmitter {} (in current state {})", threadName, gate.get());
            gate.set(false);
        }
    }

    @Override
    public Long sendAsync(final RequestContext ctx, final String asyncChannelId, final BonaPortable payload, final Long objectRef,
              final int partition, final String recordKey, final boolean isResend) {
        // redundant check to see if the channel exists (to get exception in sync thread already). Should not cost too much time due to caching
        final AsyncChannelDTO cfg = asyncTools.getCachedAsyncChannelDTO(ctx.tenantId, asyncChannelId);
        if (!cfg.getIsActive() || cfg.getAsyncQueueRef() == null) {
            LOGGER.debug("Discarding async message to inactive or unassociated channel {}", asyncChannelId);
            return null;
        }
        final Long asyncQueueRef = cfg.getAsyncQueueRef().getObjectRef();
        final QueueData queue = queueData.get(asyncQueueRef);

        if (queue != null) {
            // queue is currently active: build the in-memory message and transmit it
            final InMemoryMessage m = new InMemoryMessage();
            m.setTenantId(ctx.tenantId);       // obtain the tenantId and store it
            m.setAsyncChannelId(asyncChannelId);
            m.setObjectRef(objectRef);
            m.setPayload(payload);
            queue.kafkaWriter.write(m, partition, recordKey);
        }
        return asyncQueueRef;
    }

    protected void shutdown(final QueueData w) {
        w.shutdown(ConfigProvider.getConfiguration().getAsyncMsgConfiguration().getTimeoutShutdown());
    }

    @Override
    public void close() {
        for (final QueueData w: queueData.values()) {
            shutdown(w);
        }
        queueData.clear();
    }

    @Override
    public void close(final Long queueRef) {
        final QueueData w = queueData.get(queueRef);
        if (w != null) {
            shutdown(w);
            queueData.remove(queueRef);
        } else {
            LOGGER.error("Cannot find queue data for {}", queueRef);
        }
    }

    @Override
    public void clearQueue(final Long queueRef) {
        if (queueRef == null) {
            for (final QueueData w: queueData.values()) {
                w.writerThread.clearQueue();
            }
        } else {
            final QueueData w = queueData.get(queueRef);
            if (w != null) {
                w.writerThread.clearQueue();
            } else {
                LOGGER.error("Cannot find queue data for {}", queueRef);
            }
        }
    }

    @Override
    public void open(final AsyncQueueDTO queue) {
        queueData.computeIfAbsent(queue.getObjectRef(), (x) -> new QueueData(queue));
    }

    @Override
    public QueueStatus getQueueStatus(final Long queueRef, final String queueId) {
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

    /** For the kafka implementation, usually the messages are not persisted, but this is configurable. */
    @Override
    public boolean persistInDb() {
        return writeAllToDatabase;
    }
}
