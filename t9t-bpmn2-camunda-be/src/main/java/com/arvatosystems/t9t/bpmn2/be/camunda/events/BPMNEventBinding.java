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
package com.arvatosystems.t9t.bpmn2.be.camunda.events;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

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

public class BPMNEventBinding implements IEventHandler {

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final List<BindingBuilder<EventParameters>> builder = new LinkedList<>();

    protected final <T extends EventParameters> BindingBuilder<T> withEvent(Class<T> eventType) {
        final BindingBuilder result = new BindingBuilder(eventType);
        builder.add(result);
        return result;
    }

    @Override
    public final int execute(RequestContext ctx, EventParameters eventData) {
        for (BindingBuilder<EventParameters> b : builder) {
            if (b.match(eventData)) {
                executor.executeSynchronousAndCheckResult(b.apply(eventData), ServiceResponse.class);
            }
        }

        return 0;
    }

    public static class BindingBuilder<T extends EventParameters> {

        private Predicate<T> filterChain;
        private Function<T, String> businessKeyFunction = BindingBuilder::getDefaultBusinessKey;
        private Function<T, String> messageNameFunction = BindingBuilder::getDefaultMessageName;
        private BiConsumer<T, Map<String, Object>> variablePopulator = (event, variables) -> {
        };

        BindingBuilder(Class<T> eventType) {
            filterChain = (e) -> (eventType.isAssignableFrom(e.getClass()));
        }

        public BindingBuilder<T> filter(Predicate<T> condition) {
            filterChain = filterChain.and(condition);
            return this;
        }

        public BindingBuilder<T> businessKey(Function<T, String> businessKeyFunction) {
            this.businessKeyFunction = businessKeyFunction;
            return this;
        }

        public BindingBuilder<T> withoutBusinessKey() {
            return businessKey(e -> null);
        }

        public BindingBuilder<T> businessKey(String businessKey) {
            return businessKey(e -> businessKey);
        }

        public BindingBuilder<T> messageName(Function<T, String> messageNameFunction) {
            this.messageNameFunction = messageNameFunction;
            return this;
        }

        public BindingBuilder<T> messageName(String name) {
            return messageName(e -> name);
        }

        public BindingBuilder<T> variables(Function<T, Map<String, Object>> variablesFunction) {
            variablePopulator = variablePopulator.andThen((event, variableMap) -> variableMap.putAll(variablesFunction.apply(event)));
            return this;
        }

        public BindingBuilder<T> variable(String name, Function<T, Object> valueFunction) {
            variablePopulator = variablePopulator.andThen((event, variableMap) -> variableMap.put(name, valueFunction.apply(event)));
            return this;
        }

        boolean match(T event) {
            return filterChain.test(event);
        }

        DeliverMessageRequest apply(T event) {
            final DeliverMessageRequest request = new DeliverMessageRequest();
            request.setBusinessKey(businessKeyFunction.apply(event));
            request.setMessageName(messageNameFunction.apply(event));
            final Map<String, Object> variables = new HashMap<>();
            variablePopulator.accept(event, variables);
            request.setVariables(variables);
            return request;
        }

        private static String getDefaultBusinessKey(EventParameters eventData) {
            String businessKey = null;

            if (eventData instanceof IHasBusinessKey) {
                businessKey = ((IHasBusinessKey) eventData).getBusinessKey();
            }

            return businessKey;
        }

        private static String getDefaultMessageName(EventParameters eventData) {
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
    }

}
