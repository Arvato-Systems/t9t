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
package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.dp.Jdp;

/**
 * Utilized by {@link KafkaSimplePartitionOrderedRequestProcessor} to process a batch of records (which belong to same partition).
 */
final class KafkaSimpleMultipleRecordsProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSimpleMultipleRecordsProcessor.class);

    private final IUnauthenticatedServiceRequestExecutor requestProcessor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);

    // controls to handle threaded processing
    private final ReentrantLock processingLock = new ReentrantLock();
    private final AtomicBoolean startedProcessing = new AtomicBoolean(false);
    private final AtomicBoolean stoppedProcessing = new AtomicBoolean(false);
    private final AtomicBoolean finishedProcessing = new AtomicBoolean(false);
    private final CompletableFuture<Long> completion = new CompletableFuture<>();
    private final AtomicInteger recordsProcessed = new AtomicInteger(0);

    private final AuthenticationParameters defaultAuthHeader;
    private final AtomicInteger uniqueRequestsCounter = new AtomicInteger(0);
    private final AtomicLong lastProcessedOffset = new AtomicLong(-1);
    private final List<ConsumerRecord<String, byte[]>> records;
    private final TopicPartition topicPartition;

    KafkaSimpleMultipleRecordsProcessor(final TopicPartition topicPartition, final List<ConsumerRecord<String, byte[]>> records,
            final AuthenticationParameters defaultAuthHeader) {
        this.topicPartition = topicPartition;
        this.records = records;
        this.defaultAuthHeader = defaultAuthHeader;

        LOGGER.debug("{} started for partition={}, topic={}, #records={}", this.getClass().getSimpleName(), topicPartition.partition(), topicPartition.topic(),
                records.size());
    }

    @Override
    public void run() {
        processingLock.lock();
        try {
            if (stoppedProcessing.get()) {
                return;
            }
            startedProcessing.set(true);
        } finally {
            processingLock.unlock();
        }

        for (final ConsumerRecord<String, byte[]> consumerRecord : records) {
            if (stoppedProcessing.get()) {
                break;
            }
            processRequest(consumerRecord);
            lastProcessedOffset.set(consumerRecord.offset() + 1); // according to documentation: + 1
            recordsProcessed.incrementAndGet();
        }

        finishedProcessing.set(true);
        completion.complete(lastProcessedOffset.get());
    }

    public void stopProcessing() {
        processingLock.lock();
        try {
            stoppedProcessing.set(true);
            if (!startedProcessing.get()) {
                // not even started? Then mark as finished and leave records unprocessed
                finishedProcessing.set(true);
                completion.complete(-1L);
            }
        } finally {
            processingLock.unlock();
        }
    }

    public long waitForCompletion() {
        try {
            return completion.get();
        } catch (final CancellationException | InterruptedException | ExecutionException exc) {
            return -1;
        }
    }

    public TopicPartition getPartition() {
        return topicPartition;
    }

    public int getNumPending() {
        return records.size() - recordsProcessed.get();
    }

    public int getNumRecords() {
        return records.size();
    }

    public boolean isFinished() {
        return finishedProcessing.get();
    }

    public long getLastProcessedOffset() {
        return lastProcessedOffset.get();
    }

    private void processRequest(final ConsumerRecord<String, byte[]> oneRecord) {
        final ServiceRequest serviceRequest = deserialize(oneRecord);
        if (serviceRequest == null) {
            return;
        }

        serviceRequest.setPartitionUsed(oneRecord.partition());

        final RequestParameters requestParameters = serviceRequest.getRequestParameters();
        final ServiceRequestHeader optHdr = serviceRequest.getRequestHeader();
        final UUID messageId = requestParameters.getMessageId() == null ? optHdr != null ? optHdr.getMessageId() : null : requestParameters.getMessageId();
        if (serviceRequest.getAuthentication() == null) {
            // no specific user provided: use default (consider security!) or fail, if none configured (default)
            if (defaultAuthHeader == null) {
                LOGGER.error("Received request {} (ID {}) for {} without authentication header", requestParameters.ret$PQON(), messageId,
                        requestParameters.getEssentialKey());
                return;
            }
            serviceRequest.setAuthentication(defaultAuthHeader);
        }
        if (requestParameters.getTransactionOriginType() == null) {
            requestParameters.setTransactionOriginType(TransactionOriginType.KAFKA); // some other kafka based source
        }
        final int uniqueId = uniqueRequestsCounter.incrementAndGet();

        LOGGER.debug("EXECUTING task {}/{}/{} (ID/PART/OFFSET) (MSG-ID={}, PQON={}, essentialKey={}", uniqueId, oneRecord.partition(), oneRecord.offset(),
                messageId, requestParameters.ret$PQON(), requestParameters.getEssentialKey());
        try {
            requestProcessor.execute(serviceRequest);
            LOGGER.trace("COMPLETED task {}/{}/{} (ID/PART/OFFSET) (MSG-ID={}, PQON={}, essentialKey={}", uniqueId, oneRecord.partition(), oneRecord.offset(),
                    messageId, requestParameters.ret$PQON(), requestParameters.getEssentialKey());
        } catch (final Exception exc) {
            LOGGER.error("FAILED task {}/{}/{} (ID/PART/OFFSET) (MSG-ID={}, PQON={}, essentialKey={}  due to {}:{}", uniqueId, oneRecord.partition(),
                    oneRecord.offset(), messageId, requestParameters.ret$PQON(), requestParameters.getEssentialKey(), exc.getClass().getSimpleName(),
                    exc.getMessage());
        }
    }

    private ServiceRequest deserialize(final ConsumerRecord<String, byte[]> oneRecord) {
        final BonaPortable obj;
        try {
            obj = new CompactByteArrayParser(oneRecord.value(), 0, -1).readRecord();
        } catch (final Exception exc) {
            LOGGER.error("Data in topic {} for key {} is not a parseable BonaPortable: {}", oneRecord.topic(), oneRecord.key(), exc.getMessage());
            return null;
        }
        if (obj == null || !ServiceRequest.class.isAssignableFrom(obj.getClass())) {
            LOGGER.error("Data in topic {} for key {} is not the expected type {}, but {}", oneRecord.topic(), oneRecord.key(),
                    ServiceRequest.class.getCanonicalName(), obj == null ? "NULL" : obj.getClass().getCanonicalName());
        }

        return (ServiceRequest) obj;
    }

}
