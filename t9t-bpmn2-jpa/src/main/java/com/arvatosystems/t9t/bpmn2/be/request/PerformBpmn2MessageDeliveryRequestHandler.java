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
package com.arvatosystems.t9t.bpmn2.be.request;

import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.join;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.Bpmn2MessagePayloadDTO;
import com.arvatosystems.t9t.bpmn2.jpa.entities.Bpmn2MessageQueueEntity;
import com.arvatosystems.t9t.bpmn2.jpa.persistence.IBpmn2MessageQueueEntityResolver;
import com.arvatosystems.t9t.bpmn2.request.DeliverMessageRequest;
import com.arvatosystems.t9t.bpmn2.request.PerformBpmn2MessageDeliveryRequest;

import de.jpaw.dp.Jdp;

public class PerformBpmn2MessageDeliveryRequestHandler extends AbstractRequestHandler<PerformBpmn2MessageDeliveryRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerformBpmn2MessageDeliveryRequestHandler.class);

    // For performance sake, we use direct access to the resolver instead having needed methods as services in SAPI,
    // since this would require DTO<->Entity-Mappings
    private final IBpmn2MessageQueueEntityResolver resolver = Jdp.getRequired(IBpmn2MessageQueueEntityResolver.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, PerformBpmn2MessageDeliveryRequest request) throws Exception {

        final List<Bpmn2MessageQueueEntity> messageQueue = getMessagesForRedelivery(request);

        for (Bpmn2MessageQueueEntity messageEntry : messageQueue) {
            final DeliverMessageRequest messageDeliveryRequest = createDeliverMessageRequest(messageEntry);
            final ServiceResponse messageDeliveryResponse = executor.executeSynchronous(messageDeliveryRequest);

            if (messageDeliveryResponse.getReturnCode() != 0) {
                markDeliveryAsFailed(messageEntry, messageDeliveryResponse);
            } else {
                LOGGER.debug("Remove message ref {} from queue", messageEntry.getObjectRef());
                resolver.remove(messageEntry);
            }
        }

        return ok();
    }

    private List<Bpmn2MessageQueueEntity> getMessagesForRedelivery(PerformBpmn2MessageDeliveryRequest request) {
        final List<Bpmn2MessageQueueEntity> messageQueue;

        if (request.getMessageQueueRef() != null) {
            LOGGER.info("Try message redelivery of provided message queue ref {}", request.getMessageQueueRef().getObjectRef());
            messageQueue = Arrays.asList(resolver.find(request.getMessageQueueRef().getObjectRef()));
        } else {
            LOGGER.info("Try message redelivery of pending messages from queue (limit to {} messaged)", request.getMessageChunkSize());
            messageQueue = getMessagesFromQueueTable(request.getMessageChunkSize());
            LOGGER.info("{} pending messaged found for redelivery", messageQueue.size());
        }

        return messageQueue;
    }

    private void markDeliveryAsFailed(Bpmn2MessageQueueEntity messageEntry, ServiceResponse messageDeliveryResponse) {
        messageEntry.setReturnCode(messageDeliveryResponse.getReturnCode());
        messageEntry.setErrorDetails(messageDeliveryResponse.getErrorDetails());

        if (messageEntry.getRetryCounter() != null && messageEntry.getRetryCounter() > 1) {
            messageEntry.setRetryCounter(messageEntry.getRetryCounter() - 1);
            LOGGER.info("Delivery of queued message ref {} (message {} with business key {}) failed - {} retries left",
                         messageEntry.getObjectRef(),
                         messageEntry.getMessageName(),
                         messageEntry.getBusinessKey(),
                         messageEntry.getRetryCounter());
        } else {
            messageEntry.setRetryCounter(null);
            LOGGER.error("Delivery of queued message ref {} (message {} with business key {}) failed - no retries left!",
                         messageEntry.getObjectRef(),
                         messageEntry.getMessageName(),
                         messageEntry.getBusinessKey());
        }
    }

    private DeliverMessageRequest createDeliverMessageRequest(Bpmn2MessageQueueEntity messageEntry) {
        final DeliverMessageRequest messageDeliveryRequest = new DeliverMessageRequest();

        messageDeliveryRequest.setMessageName(messageEntry.getMessageName());
        messageDeliveryRequest.setBusinessKey(messageEntry.getBusinessKey());
        messageDeliveryRequest.setDoNotQueue(true); // Since message is already queue, do not create another entry!

        final Bpmn2MessagePayloadDTO payload = messageEntry.getPayload();

        if (payload != null && payload.getVariables() != null) {
            messageDeliveryRequest.setVariables(payload.getVariables());
        } else {
            messageDeliveryRequest.setVariables(emptyMap());
        }

        return messageDeliveryRequest;
    }

    private List<Bpmn2MessageQueueEntity> getMessagesFromQueueTable(Integer chunkSize) {
        final EntityManager entityManager = resolver.getEntityManager();

        TypedQuery<Bpmn2MessageQueueEntity> query = entityManager.createQuery(join(" SELECT ", resolver.getEntityClass().getName(), " e",
                                                                                   " WHERE e.tenantRef = :tenantRef",
                                                                                   " AND   e.retryCounter IS NOT NULL"),
                                                                              Bpmn2MessageQueueEntity.class)
                                                                 .setParameter("tenantRef", resolver.getSharedTenantRef());

        if (chunkSize != null) {
            query = query.setMaxResults(chunkSize);
        }

        return query.getResultList();
    }

}
