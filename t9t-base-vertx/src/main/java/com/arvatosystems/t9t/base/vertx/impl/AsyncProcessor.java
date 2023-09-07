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
package com.arvatosystems.t9t.base.vertx.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.xtext.xbase.lib.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.JwtAuthentication;
import com.arvatosystems.t9t.base.event.EventData;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.event.GenericEvent;
import com.arvatosystems.t9t.base.request.ProcessEventRequest;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.impl.EventSubscriptionCache;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Default;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;

@Singleton
@Default
public class AsyncProcessor implements IAsyncRequestProcessor {
    private static final class EventMessageHandler implements Handler<Message<Object>> {
        private final Map<String, String> qualifierByTenantId = new ConcurrentHashMap<>();
        private final String eventID;

        private EventMessageHandler(final String eventID) {
            this.eventID = eventID;
        }

        private void addQualifierByTenantId(final String tenantId, final String qualifier) {
            this.qualifierByTenantId.put(tenantId, qualifier);
        }

        @Override
        public void handle(final Message<Object> event) {
            final Object body = event.body();
            if (!(body instanceof BonaPortable bona)) {
                LOGGER.error("Received an async message of type {}, cannot handle, ignoring!", body == null ? "NULL" : body.getClass().getCanonicalName());
            } else {
                // we receive 2 types of messages: ServiceRequest (via method submitTask()), or EventData (via send() and publish() methods)
                if (bona instanceof EventData eventData) {
                    final String theEventId = eventData.getData().ret$PQON();
                    final String eventTenantId = eventData.getHeader().getTenantId();
                    final JwtAuthentication authJwt = new JwtAuthentication(eventData.getHeader().getEncodedJwt());

                    if (eventData.getData() instanceof GenericEvent genericEvent) {
                        final String qualifier = getQualifierForTenant(eventTenantId);
                        if (!theEventId.equals(eventID)) {
                            LOGGER.error("eventID of received message differs: {} != {}", theEventId, eventID);
                        } else {
                            if (!EventSubscriptionCache.isActive(eventID, qualifier, theEventId)) {
                                LOGGER.debug("Skipping event {} for tenant {} (not configured)", eventID, eventTenantId);
                            } else {
                                LOGGER.debug("Processing an async42 event request {}", theEventId);
                                executeEvent(qualifier, authJwt, eventData.getData(), eventData.getHeader().getInvokingProcessRef());
                            }
                        }
                    } else {
                        LOGGER.debug("Event {} for tenant {} will now trigger a ProcessEventRequest(s)", theEventId, eventTenantId);
                        executeAllEvents(eventTenantId, theEventId, authJwt, eventData.getData(), eventData.getHeader().getInvokingProcessRef());
                    }
                } else if (bona instanceof ServiceRequest srq) {
                    runInWorkerThread(myVertx, srq);
                } else {
                    LOGGER.error("Unhandled async message payload type {}, ignoring", bona == null ? "NULL" : bona.ret$PQON());
                }
            }
        }

        private String getQualifierForTenant(final String tenantId) {
            String qualifier = qualifierByTenantId.get(tenantId);
            if (qualifier == null && !tenantId.equals(T9tConstants.GLOBAL_TENANT_ID)) {
                qualifier = qualifierByTenantId.get(T9tConstants.GLOBAL_TENANT_ID);
                LOGGER.debug("No qualifier configured within tenant {}, fallback to qualifier {} available from @ tenant", tenantId, qualifier);
            }
            return qualifier;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncProcessor.class);

    private static final String ASYNC_EVENTBUS_ADDRESS       = "t9tasync";
    private static final String ASYNC_EVENTBUS_ADDRESS_LOCAL = "t9tasync-" + MessagingUtil.HOSTNAME;
    private static final String EVENTBUS_BASE_42             = "event42.";
    private static final ConcurrentMap<Pair<String, String>, Set<IEventHandler>> SUBSCRIBERS = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, EventMessageHandler> REGISTERED_HANDLER = new ConcurrentHashMap<>();

    // set a long enough timeout (30 minutes) to allow for concurrent batches
    private static final DeliveryOptions ASYNC_EVENTBUS_DELIVERY_OPTIONS = new DeliveryOptions()
            .setSendTimeout(((30 * 60) * 1000L)).setCodecName(CompactMessageCodec.COMPACT_MESSAGE_CODEC_ID);
//    private static final DeliveryOptions PUBLISH_EVENTBUS_DELIVERY_OPTIONS = new DeliveryOptions()
//            .setSendTimeout(((30 * 60) * 1000L));

    @IsLogicallyFinal
    private static EventBus bus = null;

    @IsLogicallyFinal
    private static Vertx myVertx = null;

    @IsLogicallyFinal
    private static IUnauthenticatedServiceRequestExecutor serviceRequestExecutor;

    @IsLogicallyFinal
    private static WorkerExecutor asyncExecutorPool = null;

    public AsyncProcessor() {
        LOGGER.info("vert.x AsyncProcessor implementation selected - async commands can be sent across JVMs");
    }

    private static String toBusAddress(final String eventID) {
        return EVENTBUS_BASE_42 + eventID;
    }

    @Override
    public void submitTask(final ServiceRequest request, final boolean localNodeOnly, final boolean publish) {
        LOGGER.debug("async request {} submitted via vert.x EventBus with setting local={}, allNodes={}. bus is {}",
          request.getRequestParameters().ret$PQON(), localNodeOnly, publish, bus == null ? "NULL" : "OK");
        request.freeze(); // async must freeze it to avoid subsequent modification
        if (bus != null) {
//            // ignore settings, they seem to have issues currently
//            bus.send(ASYNC_EVENTBUS_ADDRESS, request, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            if (publish) {
                bus.publish(ASYNC_EVENTBUS_ADDRESS, request, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            } else if (localNodeOnly) {
                bus.send(ASYNC_EVENTBUS_ADDRESS_LOCAL, request, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            } else {
                bus.send(ASYNC_EVENTBUS_ADDRESS, request, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            }
        }
    }

    /** Sends event data to a single subscriber. */
    @Override
    public void send(final EventData data) {
        LOGGER.debug("async event {} sent via vert.x EventBus", data.getData().ret$PQON());
        if (bus != null) {
            final EventParameters attribs = data.getData();
            if (attribs instanceof GenericEvent genericEvent) {
                bus.send(toBusAddress(genericEvent.getEventID()), data, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            } else {
                bus.send(toBusAddress(attribs.ret$PQON()), data, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            }
        } else {
            LOGGER.error("event bus is null - discarding event {}", data.getData().ret$PQON());
        }
    }

    /** Publishes event data to all subscribers. */
    @Override
    public void publish(final EventData data) {
        LOGGER.debug("async event {} published via vert.x EventBus", data.getData().ret$PQON());
        if (bus != null) {
            final EventParameters attribs = data.getData();
            if (attribs instanceof GenericEvent genericEvent) {
                bus.publish(toBusAddress(genericEvent.getEventID()), data, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            } else {
                bus.publish(toBusAddress(attribs.ret$PQON()), data, ASYNC_EVENTBUS_DELIVERY_OPTIONS);
            }
        } else {
            LOGGER.error("event bus is null - discarding event {}", data.getData().ret$PQON());
        }
    }

    /** Registers an implementation of an event handler for a given ID. */
    @Override
    public void registerSubscriber(final String eventID, final IEventHandler subscriber) {
        registerSubscriber(eventID, T9tConstants.GLOBAL_TENANT_ID, subscriber);
    }

    /** Register an IEventHandler as subscriber for an eventID. */
    @Override
    public void registerSubscriber(final String eventID, final String tenantId, final IEventHandler subscriber) {
        LOGGER.debug("Registering subscriber {} for event {} in tenant {} ...", subscriber.getClass().getCanonicalName(), eventID, tenantId);

        boolean isNewSubscriber = true;

        final Pair<String, String> key = new Pair<>(eventID, tenantId);
        final Set<IEventHandler> currentEventHandlers = SUBSCRIBERS.get(key);

        if (currentEventHandlers == null) {
            final Set<IEventHandler> newSubscriberSet = new HashSet<>();
            newSubscriberSet.add(subscriber);
            SUBSCRIBERS.put(key, newSubscriberSet);
            LOGGER.debug("  (this is the initial handler for this event)");
        } else if (!currentEventHandlers.contains(subscriber)) {
            currentEventHandlers.add(subscriber);
            SUBSCRIBERS.put(key, currentEventHandlers);
            LOGGER.debug("  (this event now has {} registered handlers)", SUBSCRIBERS.size());
        } else {
            isNewSubscriber = false;
            LOGGER.info("Subscriber {} already registered for event {} in tenant {}. Skip this one.", subscriber, eventID, tenantId);
        }

        if (isNewSubscriber && bus != null) {
            addConsumer(eventID, tenantId, subscriber);
        }
    }

    /** Called only if the bus has been set up before, to register a newly provided subscriber */
    private static void addConsumer(final String eventID, final String tenantId, final IEventHandler subscriber) {
        // get the qualifier of the subscriber
        final Named annotationNamed = subscriber.getClass().getAnnotation(Named.class);
        final String qualifier = annotationNamed == null ? null : annotationNamed.value();

        // get handler instance
        final EventMessageHandler handler = REGISTERED_HANDLER.computeIfAbsent(toBusAddress(eventID), (final String busAddress) -> {
            final EventMessageHandler eventMessageHandler = new EventMessageHandler(eventID);
            final MessageConsumer<Object> consumer = bus.consumer(busAddress);
            consumer.completionHandler((final AsyncResult<Void> asyncResult) -> {
                if (asyncResult.succeeded()) {
                    LOGGER.info("vertx async event42 handler successfully registered on eventbus address {}", busAddress);
                } else {
                    LOGGER.error("vertx async event42 handler FAILED to register on event bus at {}", busAddress);
                }
            });
            consumer.handler(eventMessageHandler);
            return eventMessageHandler;
        });

        // add tenant to qualifier mapping to handler for but address
        LOGGER.info("vertx async event42 handler {} added with qualifier {} for tenant {}", subscriber.getClass().getSimpleName(), qualifier, tenantId);
        handler.addQualifierByTenantId(tenantId, qualifier);
    }

    private static void executeEvent(final String qualifier, final JwtAuthentication authenticationJwt, final EventParameters eventParams,
            final Long invokingProcessRef) {
        final ProcessEventRequest rq = new ProcessEventRequest();
        rq.setEventHandlerQualifier(qualifier);
        rq.setEventData(eventParams);

        final ServiceRequest srq = new ServiceRequest();
        srq.setRequestParameters(rq);
        srq.setAuthentication(authenticationJwt);

        if (invokingProcessRef != null) {
            final ServiceRequestHeader hdr = new ServiceRequestHeader();
            hdr.setInvokingProcessRef(invokingProcessRef);
            srq.setRequestHeader(hdr);
        }
        runInWorkerThread(myVertx, srq);
    }

    private static void executeAllEvents(final String tenantId, final String eventId, final JwtAuthentication authenticationJwt,
            final EventParameters eventParams, final Long invokingProcessRef) {
        final Pair<String, String> key = new Pair<>(eventId, tenantId);
        Set<IEventHandler> subscribers = SUBSCRIBERS.get(key);
        if ((subscribers == null || subscribers.isEmpty()) && !T9tConstants.GLOBAL_TENANT_ID.equals(tenantId)) {
            subscribers = SUBSCRIBERS.get(new Pair<>(eventId, T9tConstants.GLOBAL_TENANT_ID));
        }
        if (subscribers == null) {
            LOGGER.debug("No subscribers registered for event ID {} - nothing to do", eventId);
            return; // nothing to do
        }

        for (IEventHandler subscriber: subscribers) {
            LOGGER.debug("Invoking event handler {}", subscriber.getClass().getCanonicalName());
            final Named annotationNamed = subscriber.getClass().getAnnotation(Named.class);
            final String qualifier = annotationNamed == null ? null : annotationNamed.value();
            final ProcessEventRequest rq = new ProcessEventRequest();
            rq.setEventHandlerQualifier(qualifier);
            rq.setEventData(eventParams);

            final ServiceRequest srq = new ServiceRequest();
            srq.setRequestParameters(rq);
            srq.setAuthentication(authenticationJwt);

            if (invokingProcessRef != null) {
                final ServiceRequestHeader hdr = new ServiceRequestHeader();
                hdr.setInvokingProcessRef(invokingProcessRef);
                srq.setRequestHeader(hdr);
            }
            runInWorkerThread(myVertx, srq);
        }
    }

    public static void runInWorkerThread(final Vertx vertx, final ServiceRequest msgBody) {
        final Handler<Promise<ServiceResponse>> blockingCodeHandler = (final Promise<ServiceResponse> promise) -> {
            promise.complete(serviceRequestExecutor.executeTrusted(msgBody));
        };
        final Handler<AsyncResult<ServiceResponse>> resultHandler = (final AsyncResult<ServiceResponse> asyncResult) -> {
            if (asyncResult.succeeded()) {
                final ServiceResponse result = asyncResult.result();
                if (!ApplicationException.isOk(result.getReturnCode())) {
                    LOGGER.error("Async request {} FAILED with return code {}, {} ({})", msgBody.ret$PQON(),
                        result.getReturnCode(), result.getErrorDetails(), result.getErrorMessage());
                }
            } else {
                LOGGER.error("Async request {} FAILED (worker thread terminated abnormally))", msgBody.ret$PQON());
            }
        };

        if (asyncExecutorPool != null && msgBody.getRequestHeader() != null && Boolean.FALSE.equals(msgBody.getRequestHeader().getPriorityRequest())) {
            asyncExecutorPool.executeBlocking(blockingCodeHandler, false, resultHandler);
        } else {
            // no separate pool configured, or priority request
            vertx.executeBlocking(blockingCodeHandler, false, resultHandler);
        }
    }

    public static void register(final Vertx vertx) {
        myVertx = vertx;
        bus = vertx.eventBus();
        bus.registerCodec(new CompactMessageCodec());
        serviceRequestExecutor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);

        Integer asyncPoolSize = ConfigProvider.getConfiguration().getApplicationConfiguration() == null ? null
                : ConfigProvider.getConfiguration().getApplicationConfiguration().getLocalAsyncPoolSize();
        if (asyncPoolSize == null) {
            LOGGER.info("Sharing executor pool with sync requests");
        } else {
            LOGGER.info("Using separate executor pool of {} threads for async requests", asyncPoolSize);
            asyncExecutorPool = vertx.createSharedWorkerExecutor("t9t-async-worker", asyncPoolSize);
        }

        registerOnBus(ASYNC_EVENTBUS_ADDRESS, "all nodes");
        registerOnBus(ASYNC_EVENTBUS_ADDRESS_LOCAL, "LOCAL");

        // now also register any preregistered event handlers
        for (final Map.Entry<Pair<String, String>, Set<IEventHandler>> q : SUBSCRIBERS.entrySet()) {
            final Set<IEventHandler> value = q.getValue();
            for (final IEventHandler eh : value) {
                addConsumer(q.getKey().getKey(), q.getKey().getValue(), eh);
            }
        }
    }

    private static void registerOnBus(final String address, final String what) {
        final MessageConsumer<Object> consumer = bus.consumer(address);
        consumer.completionHandler((final AsyncResult<Void> asyncResult) -> {
            if (asyncResult.succeeded()) {
                LOGGER.info("vertx async request processor successfully registered on {} eventbus address {}", what, address);
            } else {
                LOGGER.error("vertx async request processor FAILED");
            }
        });
        consumer.handler((Message<Object> message) -> {
            final Object msgBody = message.body();
            if (msgBody instanceof ServiceRequest serviceRequest) {
                LOGGER.debug("Processing an async request {} from {}", serviceRequest.ret$PQON(), what);
                runInWorkerThread(myVertx, serviceRequest);
            } else {
                LOGGER.error("Received an async message of type {} on {}, cannot handle!", msgBody.getClass().getCanonicalName(), what);
            }
        });
    }
}
