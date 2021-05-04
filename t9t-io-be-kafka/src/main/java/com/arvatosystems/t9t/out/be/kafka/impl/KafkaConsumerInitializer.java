package com.arvatosystems.t9t.out.be.kafka.impl;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.KafkaConfiguration;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.event.DataSinkChangedEvent;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;
import de.jpaw.util.ExceptionUtil;

@Startup(95001)
@Singleton
public class KafkaConsumerInitializer implements StartupShutdown {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerInitializer.class);
    public static final String KAFKA_INPUT_SESSIONS_GROUP_ID = "InputSession";
    public static final int DEFAULT_WORKER_POOL_SIZE = 4;
    public static final int DEFAULT_COMMIT_ASYNC_EVERY_N = 10;  // send an async commit every 10th executed command
    public static final long DEFAULT_TIMEOUT = 9999L;  // any request must terminate within 10 seconds

    protected final IOutPersistenceAccess iOutPersistenceAccess = Jdp.getRequired(IOutPersistenceAccess.class);
    protected final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);

    protected final Map<String, DataSinkDTO> dataSinkByTopic     = new ConcurrentHashMap<>(20);
    protected final Map<Long,   DataSinkDTO> dataSinkByObjectRef = new ConcurrentHashMap<>(20);
    protected final Map<Long,   IInputSession> inputSessionByDataSinkByObjectRef = new ConcurrentHashMap<>(20);

    @IsLogicallyFinal  // set by open() method
    private UUID defaultApiKey;
    @IsLogicallyFinal  // set by open() method
    private ExecutorService executorKafkaIO;
    @IsLogicallyFinal  // set by open() method
    private Future<Boolean> writerResult;
    @IsLogicallyFinal  // set by open() method
    private KafkaConfiguration defaults;
    @IsLogicallyFinal  // set by open() method
    private Consumer<String, byte[]> consumer;
    @IsLogicallyFinal  // set by open() method
    private boolean useTopicPattern;
    @IsLogicallyFinal
    private int workerPoolSize;
    @IsLogicallyFinal
    private volatile boolean pleaseStop = false;

    // simple storage class to avoid using a Pair<>
    static private class FutureOffset {
        final Future<?> f;
        final long offset;
        private FutureOffset(Future<?> f, long offset) {
            this.f = f;
            this.offset = offset;
        }
    }

    private class WriterThread implements Callable<Boolean> {
        private final Map<TopicPartition, FutureOffset> pendingRequests = new HashMap<>(100); // map to ensure no 2 requests are executed for the same topic/partition
        private final Map<TopicPartition, OffsetAndMetadata> finishedRequests = new HashMap<>(100);
        private final AtomicInteger workerThreadCounter = new AtomicInteger();
        private final ExecutorService executorKafkaWorker = Executors.newFixedThreadPool(workerPoolSize, (threadFactory) -> {
            final String threadName = "t9t-KafkaWorker-" + workerThreadCounter.incrementAndGet();
            LOGGER.info("Launching thread {} of {} for kafka worker", threadName, workerPoolSize);
            return new Thread(threadFactory, threadName);
        });

        private Future<?> processRecord(String topic, String key, byte[] data) {
            LOGGER.debug("Processing record {} in topic {}", key, topic); // , new String(data, StandardCharsets.UTF_8));  // FIXME: remove extensive log
            final DataSinkDTO cfg = dataSinkByTopic.get(topic);
            if (cfg == null) {
                LOGGER.error("No data sink registered for topic!");  // have unsubscribed?
                return null;
            }
            final UUID apiKey = cfg.getApiKey() == null ? defaultApiKey : cfg.getApiKey();
            final IInputSession inputSession = inputSessionByDataSinkByObjectRef.computeIfAbsent(cfg.getObjectRef(), (x) -> {
                LOGGER.info("Opening new input session for topic {} (data sink {})", topic, cfg.getDataSinkId());
                final IInputSession is = Jdp.getRequired(IInputSession.class);
                is.open(cfg.getDataSinkId(), apiKey, topic, null);
                return is;
            });
            return executorKafkaWorker.submit(() -> inputSession.process(new ByteArrayInputStream(data)));
        }

        private void sendAsyncCommit() {
            LOGGER.debug("Sending async commit for {} records", finishedRequests.size());
            consumer.commitAsync(finishedRequests, null);
            finishedRequests.clear();
        }
        private void waitAndCommit(TopicPartition tp, FutureOffset fo, boolean commitIfEnoughPending) {
            try {
                fo.f.get(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
                pendingRequests.remove(tp);
                // great, this one has finished, perform an AsyncCommit for it
                finishedRequests.put(tp, new OffsetAndMetadata(fo.offset + 1L));
                if (commitIfEnoughPending && finishedRequests.size() >= DEFAULT_COMMIT_ASYNC_EVERY_N) {
                    sendAsyncCommit();
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                LOGGER.error("There is a problem, task not finished within timeout for topic {}: {}", tp.topic(), e.getClass().getSimpleName());
                // FIXME: should perform an "error request"
            }
        }

        private void checkForFinishedRequests() {
            for (Map.Entry<TopicPartition, FutureOffset> pendingRequest: pendingRequests.entrySet()) {
                if (pendingRequest.getValue().f.isDone()) {
                    waitAndCommit(pendingRequest.getKey(), pendingRequest.getValue(), false);
                }
            }
        }

        @Override
        public Boolean call() {
            consumer = createKafkaConsumer(defaults);
            if (useTopicPattern) {
                consumer.subscribe(Pattern.compile(defaults.getTopicPattern()));
            } else {
                consumer.subscribe(dataSinkByTopic.keySet());
            }
            while (!pleaseStop) {
                finishedRequests.clear();
                // LOGGER.debug("Start polling...");  // FIXME: remove extensive log
                final ConsumerRecords<String, byte []> records = consumer.poll(Duration.ofMillis(100));
                if (!records.isEmpty()) {
                    LOGGER.debug("Received {} data records via kafka", records.count());
                    for (ConsumerRecord<String, byte []> record : records) {
                        // process the records, somewhat in parallel, but never submit a record of a given topic/partition while the prior
                        // of the same topic/partition is still processing (this is to ensure strict ordering).
                        final TopicPartition tp = new TopicPartition(record.topic(), record.partition());
                        // get any previous execution / wait for it to complete
                        final FutureOffset fo = pendingRequests.get(tp);
                        if (fo != null) {
                            waitAndCommit(tp, fo, true);
                        }
                        try {
                            final Future<?> f = processRecord(record.topic(), record.key(), record.value());
                            if (f != null) {
                                // store it in the map
                                pendingRequests.put(tp, new FutureOffset(f, record.offset()));
                            }
                        } catch (Exception e) {
                            LOGGER.error("Exception occurred:", e);
                            LOGGER.error("Could not process request: caught {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                        }
                    }
                    // all records submitted for execution.
                    // now check if there are any which completed and can be committed
                    checkForFinishedRequests();
                    sendAsyncCommit();
                }
            }
            LOGGER.info("End requested, waiting for all pending commands...");
            try {
                executorKafkaWorker.awaitTermination(10000L, TimeUnit.MILLISECONDS);
                LOGGER.info("Shutting down executor pool...");
                executorKafkaWorker.shutdown();
                LOGGER.info("All tasks terminated, committing SYNC before ending thread...");
                consumer.commitSync(Duration.ofMillis(2000l));
            } catch (InterruptedException e) {
                LOGGER.error("There is a problem, tasks not finished within shutdown timeout: {}", e.getClass().getSimpleName());
                // only submit the ones which have been processed
                checkForFinishedRequests();
                LOGGER.debug("Sending sync commit for {} records (out of {})", finishedRequests.size(), pendingRequests.size());
                consumer.commitSync(finishedRequests, null);
            }
            consumer.close(Duration.ofMillis(2000l));
            return Boolean.TRUE;
        }
    }

    protected static Consumer<String, byte[]> createKafkaConsumer(final KafkaConfiguration defaults) {
        final Map<String, Object> props = new HashMap<>(10);
        final String defaultBootstrapServers = defaults == null ? null : defaults.getDefaultBootstrapServers();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_INPUT_SESSIONS_GROUP_ID);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, Boolean.FALSE);  // or "false" as found in examples?
        return new KafkaConsumer<>(props, new StringDeserializer(), new ByteArrayDeserializer());
    }

    @Override
    public void onStartup() {
        defaults = ConfigProvider.getConfiguration().getKafkaConfiguration();
        if (defaults == null || defaults.getDefaultBootstrapServers() == null) {
            LOGGER.info("No Kafka input initialization - missing or incomplete Kafka configuration in config XML: bootstrap servers or API key missing");
            return;
        }
        defaultApiKey = defaults.getDefaultImportApiKey();
        final String topicPattern = defaults.getTopicPattern();
        useTopicPattern = topicPattern != null && !topicPattern.isEmpty();
        final List<DataSinkDTO> dataSinkDTOList = iOutPersistenceAccess.getDataSinkDTOsForChannel(CommunicationTargetChannelType.KAFKA);
        if (dataSinkDTOList.isEmpty() && (topicPattern == null || topicPattern.isEmpty())) {
            LOGGER.info("No Kafka input data sinks encountered and no default topic pattern configured in config.xml");
            return;
        }
        if (defaults.getSeparateWorkerPoolSize() != null && defaults.getSeparateWorkerPoolSize().intValue() > 0) {
            workerPoolSize = defaults.getSeparateWorkerPoolSize();
        } else {
            workerPoolSize = DEFAULT_WORKER_POOL_SIZE;
        }
        LOGGER.info("Setting up Kafka consumers for {} data sinks", dataSinkDTOList.size());
        for (DataSinkDTO dataSinkDTO : dataSinkDTOList) {
            if (defaultApiKey == null && dataSinkDTO.getApiKey() == null) {
                // no processing possible, because authentication information missing
                LOGGER.error("skipping Kafka dataSink {}: no API-Key provided (no default and not in DataSinkDTO)", dataSinkDTO.getDataSinkId());
            } else {
                dataSinkDTO.freeze();
                dataSinkByTopic.put(dataSinkDTO.getFileOrQueueNamePattern(), dataSinkDTO);
                dataSinkByObjectRef.put(dataSinkDTO.getObjectRef(), dataSinkDTO);
            }
        }
        executorKafkaIO = Executors.newSingleThreadExecutor(call -> new Thread(call, "t9t-KafkaImporter"));
        writerResult = executorKafkaIO.submit(new WriterThread());

        // Register listener to receive data sink changes. The following code is a bit complicated, but we need a separate @Named instance of IEventHandler
        // to perform the change event, because the qualifier is essential to existing processing.
        final KafkaDataSinkChangeListener changeListener = Jdp.getRequired(KafkaDataSinkChangeListener.class, "IOKafkaDataSinkChange");
        // let it delegate to this class
        changeListener.setProcessor((ctx, event) -> this.execute(ctx, event));
        // and register it
        asyncProcessor.registerSubscriber(DataSinkChangedEvent.BClass.INSTANCE.getPqon(), changeListener);
    }

    @Override
    public void onShutdown() {
        if (defaults == null) {
            // there was no configuration - nothing has been initialized, not possible to shut down anything either
            return;
        }
        pleaseStop = true;
        consumer.wakeup();   // tell kafka to abort any pending poll()
        executorKafkaIO.shutdown();
        // close any open input session
        for (IInputSession imports: inputSessionByDataSinkByObjectRef.values()) {
            imports.close();
        }
    }

    // execute implements the task delegated by IEventHandler
    public void execute(RequestContext context, DataSinkChangedEvent event) {
        final DataSinkDTO dataSink = event.getDataSink();
        LOGGER.debug("Processing {} operation for data sink {}", event.getOperation(), dataSink.getDataSinkId());
        switch (event.getOperation()) {
        case ACTIVATE:
        case CREATE:
        case MERGE:
        case UPDATE:
            if (isActiveKafkaInputDataSink(dataSink)) {
                dataSinkByObjectRef.put(dataSink.getObjectRef(), dataSink);
                dataSinkByTopic.put(dataSink.getFileOrQueueNamePattern(), dataSink);
//                consumer.unsubscribe();
//                updateSubscriptions();
            } else {
                // for MERGE / update it could remove a subscription
                if (dataSinkByTopic.remove(dataSink.getFileOrQueueNamePattern()) != null) {
                    // entry existed before: must unsubscribe
//                    consumer.unsubscribe();
//                    updateSubscriptions();
                }
            }
            break;
        case DELETE:
        case INACTIVATE:
            if (isActiveKafkaInputDataSink(dataSink)) {
                // must remove this now
                dataSinkByTopic.remove(dataSink.getFileOrQueueNamePattern());
//                consumer.unsubscribe();
//                updateSubscriptions();
            }
            break;
        default:
            // nothing to do
        }
    }

//    private void updateSubscriptions() {
//        consumer.subscribe(dataSinkByTopic.keySet());
//    }

    private boolean isActiveKafkaInputDataSink(final DataSinkDTO dataSink) {
        return dataSink.getIsActive() && dataSink.getCommTargetChannelType() == CommunicationTargetChannelType.KAFKA && Boolean.TRUE == dataSink.getIsInput();
    }
}
