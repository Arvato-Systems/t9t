package com.arvatosystems.t9t.out.be.kafka.impl;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.event.EventParameters;
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

    protected final IOutPersistenceAccess iOutPersistenceAccess = Jdp.getRequired(IOutPersistenceAccess.class);
    protected final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);

    protected final Map<String, DataSinkDTO> dataSinkByTopic     = new ConcurrentHashMap<>(20);
    protected final Map<Long,   DataSinkDTO> dataSinkByObjectRef = new ConcurrentHashMap<>(20);
    protected final Map<Long,   IInputSession> inputSessionByDataSinkByObjectRef = new ConcurrentHashMap<>(20);

    @IsLogicallyFinal  // set by open() method
    private UUID defaultApiKey;
    @IsLogicallyFinal  // set by open() method
    private ExecutorService executor;
    @IsLogicallyFinal  // set by open() method
    private Future<Boolean> writerResult;
    @IsLogicallyFinal  // set by open() method
    private KafkaConfiguration defaults;
    @IsLogicallyFinal  // set by open() method
    private Consumer<String, byte[]> consumer;
    @IsLogicallyFinal
    private volatile boolean pleaseStop = false;

    private class WriterThread implements Callable<Boolean> {
        
        private void processRecord(String topic, String key, byte[] data) {
            LOGGER.debug("Processing record {} in topic {}", key, topic); // , new String(data, StandardCharsets.UTF_8));  // FIXME: remove extensive log
            final DataSinkDTO cfg = dataSinkByTopic.get(topic);
            if (cfg == null) {
                LOGGER.error("No data sink registered for topic!");  // have unsubscribed?
                return;
            }
            final IInputSession inputSession = inputSessionByDataSinkByObjectRef.computeIfAbsent(cfg.getObjectRef(), (x) -> {
                LOGGER.info("Opening new input session for topic {} (data sink {})", topic, cfg.getDataSinkId());
                final IInputSession is = Jdp.getRequired(IInputSession.class);
                is.open(cfg.getDataSinkId(), defaultApiKey, topic, null);
                return is;
            });
            inputSession.process(new ByteArrayInputStream(data));
        }

        @Override
        public Boolean call() throws Exception {
            while (!pleaseStop) {
                // LOGGER.debug("Start polling...");  // FIXME: remove extensive log
                final ConsumerRecords<String, byte []> records = consumer.poll(Duration.ofMillis(100));
                if (!records.isEmpty()) {
                    LOGGER.debug("Received {} data records via kafka", records.count());
                    for (ConsumerRecord<String, byte []> record : records) {
                        try {
                            processRecord(record.topic(), record.key(), record.value());
                        } catch (Exception e) {
                            LOGGER.error("Exception occurred:", e);
                            LOGGER.error("Could not process request: caught {}: {}", e.getMessage(), ExceptionUtil.causeChain(e));
                        }
                    }
                    LOGGER.debug("Committing...");
                    consumer.commitAsync();
                }
            }
            return Boolean.TRUE;
        }
    }

    protected static Consumer<String, byte[]> createKafkaConsumer(final KafkaConfiguration defaults) {
        final Properties props = new Properties();
        final String defaultBootstrapServers = defaults == null ? null : defaults.getDefaultBootstrapServers();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, defaultBootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_INPUT_SESSIONS_GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        return new KafkaConsumer<>(props);
    }

    @Override
    public void onStartup() {
        defaults = ConfigProvider.getConfiguration().getKafkaConfiguration();
        defaultApiKey = defaults == null ? null : defaults.getDefaultImportApiKey();
        if (defaultApiKey == null || defaults.getDefaultBootstrapServers() == null) {
            LOGGER.info("No Kafka input initialization - sing or incomplete Kafka configuration in config XML: bootstrap servers or API key missing");
            defaults = null;
            return;
        }
        final List<DataSinkDTO> dataSinkDTOList = iOutPersistenceAccess.getDataSinkDTOsForChannel(CommunicationTargetChannelType.KAFKA);
        if (dataSinkDTOList.isEmpty()) {
            LOGGER.info("No Kafka input data sinks encountered");
            return;
        }
        LOGGER.info("Setting up Kafka consumers for {} data sinks", dataSinkDTOList.size());
        consumer = createKafkaConsumer(defaults);
        for (DataSinkDTO dataSinkDTO : dataSinkDTOList) {
            dataSinkDTO.freeze();
            dataSinkByTopic.put(dataSinkDTO.getFileOrQueueNamePattern(), dataSinkDTO);
            dataSinkByObjectRef.put(dataSinkDTO.getObjectRef(), dataSinkDTO);
        }
        updateSubscriptions();
        executor = Executors.newSingleThreadExecutor(call -> new Thread(call, "t9t-KafkaImporter"));
        writerResult = executor.submit(new WriterThread());

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
        executor.shutdown();
        // close any open input session
        for (IInputSession imports: inputSessionByDataSinkByObjectRef.values()) {
            imports.close();
        }
        consumer.close(Duration.ofMillis(2000l));
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
                consumer.unsubscribe();
                updateSubscriptions();
            } else {
                // for MERGE / update it could remove a subscription
                if (dataSinkByTopic.remove(dataSink.getFileOrQueueNamePattern()) != null) {
                    // entry existed before: must unsubscribe
                    consumer.unsubscribe();
                    updateSubscriptions();
                }
            }
            break;
        case DELETE:
        case INACTIVATE:
            if (isActiveKafkaInputDataSink(dataSink)) {
                // must remove this now
                dataSinkByTopic.remove(dataSink.getFileOrQueueNamePattern());
                consumer.unsubscribe();
                updateSubscriptions();
            }
            break;
        default:
            // nothing to do
        }
    }

    private void updateSubscriptions() {
        consumer.subscribe(dataSinkByTopic.keySet());
    }

    private boolean isActiveKafkaInputDataSink(final DataSinkDTO dataSink) {
        return dataSink.getIsActive() && dataSink.getCommTargetChannelType() == CommunicationTargetChannelType.KAFKA && Boolean.TRUE == dataSink.getIsInput();
    }
}
