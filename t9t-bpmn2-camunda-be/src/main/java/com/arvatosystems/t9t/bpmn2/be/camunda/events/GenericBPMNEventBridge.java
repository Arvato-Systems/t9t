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
package com.arvatosystems.t9t.bpmn2.be.camunda.events;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.event.GenericEvent;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn2.BPMNMessageEvent;
import com.arvatosystems.t9t.bpmn2.event.IHasBusinessKey;
import com.arvatosystems.t9t.bpmn2.request.DeliverMessageRequest;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

/**
 * Event handler to provide events to BPMN engine by bridging the T9T events to the DeliverMessageRequest.
 *
 * @author TWEL006
 */
@Singleton
@Named("BPMN2GenericBridge")
public class GenericBPMNEventBridge implements IEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericBPMNEventBridge.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public final int execute(RequestContext ctx, EventParameters eventData) {
        final DeliverMessageRequest request = new DeliverMessageRequest();
        request.setMessageName(getMessageName(eventData));
        request.setBusinessKey(getBusinessKey(eventData));
        request.setVariables(getVariables(eventData));

        LOGGER.debug("Deliver message {} with business key {} (payload {})", request.getMessageName(), request.getBusinessKey(), request.getVariables());

        executor.executeSynchronousAndCheckResult(request, ServiceResponse.class);

        return 0;
    }

    protected Map<String, Object> getVariables(EventParameters eventData) {
        final Map<String, Object> variables = new HashMap<>();

        if (eventData instanceof BPMNMessageEvent) {
            final BPMNMessageEvent bpmnMessage = (BPMNMessageEvent) eventData;

            if (bpmnMessage.getVariables() != null) {
                variables.putAll(bpmnMessage.getVariables());
            }
        }

        return variables;
    }

    protected String getMessageName(EventParameters eventData) {
        String messageName = null;

        // In case of a GenericEvent, a more detailed event id might be available
        if (eventData instanceof GenericEvent) {
            messageName = ((GenericEvent) eventData).getEventID();
        } else if (eventData instanceof BPMNMessageEvent) {
            messageName = ((BPMNMessageEvent) eventData).getMessageName();
        }

        // Use PQON as fallback to guarantee an messageName
        if (messageName == null) {
            messageName = eventData.ret$PQON();
        }

        return messageName;
    }

    protected String getBusinessKey(EventParameters eventData) {
        String businessKey = null;

        if (eventData instanceof IHasBusinessKey) {
            businessKey = ((IHasBusinessKey) eventData).getBusinessKey();
        }

        return businessKey;
    }

}
