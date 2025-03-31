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
package com.arvatosystems.t9t.bpmn2.be.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.Bpmn2MessagePayloadDTO;
import com.arvatosystems.t9t.bpmn2.Bpmn2MessageQueueDTO;
import com.arvatosystems.t9t.bpmn2.T9tBPMN2Exception;
import com.arvatosystems.t9t.bpmn2.be.service.IBpmn2MessageQueueService;
import com.arvatosystems.t9t.bpmn2.request.DeliverMessageRequest;

import de.jpaw.dp.Jdp;

public class DeliverMessageRequestHandler extends AbstractBPMNRequestHandler<DeliverMessageRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeliverMessageRequestHandler.class);

    private static final int DEFAULT_BPMN_MESSAGE_REDELIVERY_COUNTER = 5;

    private final RuntimeService runtimeService = Jdp.getRequired(RuntimeService.class);
    private final IBpmn2MessageQueueService messageQueueService = Jdp.getRequired(IBpmn2MessageQueueService.class);

    @Override
    protected ServiceResponse executeInWorkflowContext(RequestContext requestContext, DeliverMessageRequest request) throws Exception {
        LOGGER.debug("Deliver message '{}' with business key '{}' - Payload: {}", request.getMessageName(), request.getBusinessKey(), request.getVariables());

        MessageCorrelationBuilder messageBuilder = createMessageCorrelation(requestContext, request);

        if (request.getBusinessKey() != null) {
            deliverSingleReceiver(request, messageBuilder);
        } else {
            deliverBroadcast(request, messageBuilder);
        }

        return ok();
    }

    private MessageCorrelationBuilder createMessageCorrelation(RequestContext requestContext, DeliverMessageRequest request) {
        MessageCorrelationBuilder messageBuilder = runtimeService.createMessageCorrelation(request.getMessageName())
                                                                 .tenantId(requestContext.tenantId);

        if (request.getBusinessKey() != null) {
            messageBuilder = messageBuilder.processInstanceBusinessKey(request.getBusinessKey());
        }

        final Map<String, Object> variables = new HashMap<>();

        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        messageBuilder = messageBuilder.setVariables(variables);

        return messageBuilder;
    }

    private void deliverBroadcast(DeliverMessageRequest request, MessageCorrelationBuilder messageBuilder) {

        // Since we do not directly address some participant, just perform some kind of broadcast with fire and
        // forget. We can not check anyway...
        LOGGER.debug("Due to no existing business key, deliver to all (or no) subscriber");
        final List<MessageCorrelationResult> result = messageBuilder.correlateAllWithResult();

        if (LOGGER.isDebugEnabled()) {
            if (result.isEmpty()) {
                LOGGER.debug("No subscriber available - message is dropped");
            } else {
                for (MessageCorrelationResult correlation : result) {
                    LOGGER.debug("Process instance id '{}' (business key '{}') {}",
                            correlation.getProcessInstance()
                                       .getId(),
                            correlation.getProcessInstance()
                                       .getBusinessKey(),
                            correlation.getResultType() == MessageCorrelationResultType.ProcessDefinition ? "started by message" : "received message");
                }
            }
        }
    }

    private void deliverSingleReceiver(DeliverMessageRequest request, MessageCorrelationBuilder messageBuilder) {
        try {

            // Send to exact one subscriber.
            LOGGER.debug("Due to existing business key, delivery message to single subscriber");
            messageBuilder.correlate();

        } catch (MismatchingMessageCorrelationException e) {
            // There is no or more than one subscriber. This is an issue, since we are using a business key to
            // directly address some participant.

            if (!Boolean.TRUE.equals(request.getDoNotQueue())) {
                queueMessage(request, e.getMessage());
            }

            throw new T9tException(T9tBPMN2Exception.BPMN2_MESSAGE_DELIVERY_FAILED, e.getMessage());
        }
    }

    private void queueMessage(DeliverMessageRequest request, String errorDetails) {
        final Bpmn2MessageQueueDTO messageQueue = new Bpmn2MessageQueueDTO();

        messageQueue.setMessageName(request.getMessageName());
        messageQueue.setBusinessKey(request.getBusinessKey());

        if (request.getVariables() != null && !request.getVariables().isEmpty()) {
            messageQueue.setPayload(new Bpmn2MessagePayloadDTO(request.getVariables()));
        }

        messageQueue.setRetryCounter(DEFAULT_BPMN_MESSAGE_REDELIVERY_COUNTER);
        messageQueue.setReturnCode(T9tBPMN2Exception.BPMN2_MESSAGE_DELIVERY_FAILED);
        messageQueue.setErrorDetails(StringUtils.left(errorDetails, 512));
        messageQueueService.queueMessage(messageQueue);
    }
}
