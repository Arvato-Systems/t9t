/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.vertx.impl

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.event.EventData
import com.arvatosystems.t9t.base.event.EventParameters
import com.arvatosystems.t9t.base.event.GenericEvent
import com.arvatosystems.t9t.base.request.ProcessEventRequest
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor
import com.arvatosystems.t9t.base.services.IEventHandler
import com.arvatosystems.t9t.base.services.impl.EventSubscriptionCache
import com.arvatosystems.t9t.base.types.AuthenticationJwt
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.core.JsonComposer
import de.jpaw.bonaparte.core.MapComposer
import de.jpaw.bonaparte.core.MapParser
import de.jpaw.bonaparte8.vertx3.CompactMessageCodec
import de.jpaw.dp.Default
import de.jpaw.dp.Jdp
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import de.jpaw.json.JsonParser
import de.jpaw.util.ApplicationException
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.WorkerExecutor
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import java.util.HashMap
import java.util.HashSet
import java.util.Map
import java.util.Set
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import com.arvatosystems.t9t.base.api.ServiceRequestHeader

@AddLogger
@Singleton
@Default
class AsyncProcessor implements IAsyncRequestProcessor {
    // private fields
    static final String ASYNC_EVENTBUS_ADDRESS = "t9tasync"
    static final String EVENTBUS_BASE_42 = "event42.";
    static EventBus bus = null;
    static Vertx myVertx = null;
    static final ConcurrentMap<Pair<String, Long>, Set<IEventHandler>> SUBSCRIBERS = new ConcurrentHashMap<Pair<String, Long>, Set<IEventHandler>>();
    static final ConcurrentMap<String, EventMessageHandler> REGISTERED_HANDLER = new ConcurrentHashMap

    static IUnauthenticatedServiceRequestExecutor serviceRequestExecutor
    static WorkerExecutor asyncExecutorPool = null  // possibly assigned during register()

    new() {
        LOGGER.info("vert.x AsyncProcessor implementation selected - async commands can be sent across JVMs");
    }

    def private static String toBusAddress(String eventID) {
        return EVENTBUS_BASE_42 + eventID
    }

    private static class EventMessageHandler implements Handler<Message<Object>> {

        String eventID
        Map<Long, String> qualifierByTenantRef = new HashMap

        override handle(Message<Object> it) {
            var JsonObject msgBody
            // first make sure we have a jsonBody to work with. Otherwise any parsing or handling will likely fail
            if (body instanceof String) {
                msgBody = new JsonObject(body as String)
            } else {
                msgBody = body as JsonObject
            }
            // then continue evaluating the message
            if (msgBody instanceof JsonObject) {
                if (msgBody.containsKey("@PQON")) { // check if the event contains custom eventParameters by searching for a PQON
                    var map = new JsonParser(msgBody.encode, false).parseObject
                    val BonaPortable tentativeEventData = MapParser.asBonaPortable(map, MapParser.OUTER_BONAPORTABLE_FOR_JSON)
                    if (tentativeEventData instanceof EventData) {
                        val theEventId = tentativeEventData.data.ret$PQON
                        val eventTenantRef = tentativeEventData.header.tenantRef
                        val qualifier = getQualifierForTenant(eventTenantRef)

                        LOGGER.debug("Event {} with qualifier {} for tenant {} will now trigger a ProcessEventRequest", theEventId, qualifier, eventTenantRef)
                        executeEvent(qualifier, new AuthenticationJwt(tentativeEventData.header.encodedJwt), tentativeEventData.data, tentativeEventData.header?.invokingProcessRef)
                    }
                } else { // otherwise deal with a raw json event without PQON
                    val theEventID = msgBody.getString("eventID")
                    val header = msgBody.getJsonObject("header")
                    val tenantRef = header?.getLong("tenantRef")
                    val z = msgBody.getJsonObject("z")
                    val qualifier = getQualifierForTenant(tenantRef)
                    if (theEventID === null || header === null || tenantRef === null) {
                        LOGGER.error("eventID or header or tenantId missing in event42 object at address {}", eventID)
                    } else if (eventID != theEventID) {
                        LOGGER.error("eventID of received message differs: {} != {}", eventID, theEventID)
                    } else {
                        if (!EventSubscriptionCache.isActive(eventID, qualifier, tenantRef)) {
                            LOGGER.debug("Skipping event {} for tenant {} (not configured)", eventID, tenantRef)
                        } else {
                            LOGGER.debug("Processing an async42 event request {}", theEventID)
                            // pre checks good. construct a request for it
                            executeEvent(qualifier, new AuthenticationJwt(header.getString("encodedJwt")), new GenericEvent => [
                                    it.eventID = eventID
                                    it.z = z?.map
                                ], header.getLong("invokingProcessRef"))
                        }
                    }
                }
            } else {
                LOGGER.error("Received an async message of type {}, cannot handle!", msgBody.class.canonicalName)
            }
        }

        private def String getQualifierForTenant(Long tenantRef) {
            var qualifier = qualifierByTenantRef.get(tenantRef)

            // If tenant ref is not the @ tenant and no qualifier is registered, fallback to @ tenants qualifier configuration
            if (qualifier === null && tenantRef != T9tConstants.GLOBAL_TENANT_REF42) {
                qualifier = qualifierByTenantRef.get(T9tConstants.GLOBAL_TENANT_REF42)
                LOGGER.debug("No qualifier configured within tenant {}, fallback to qualifier {} available from @ tenant", tenantRef, qualifier)
            }

            return qualifier;
        }

    }

    /** Called only if the bus has been set up before, to register a newly provided subscriber */
    def private static void addConsumer(String eventID, Long tenantRef, IEventHandler subscriber) {
        // get the qualifier of the subscriber
        val qualifier = subscriber.class.getAnnotation(Named)?.value

        // get handler instance
        val handler = REGISTERED_HANDLER.computeIfAbsent(eventID.toBusAddress,
                                                         [busAddress|
                                                             val handler = new EventMessageHandler => [
                                                                 it.eventID = eventID
                                                             ]
                                                             val consumer = bus.consumer(busAddress)
                                                             consumer.completionHandler [
                                                                 if (succeeded)
                                                                     LOGGER.info("vertx async event42 handler successfully registered on eventbus address {}", busAddress)
                                                                 else
                                                                     LOGGER.error("vertx async event42 handler FAILED to register on event bus at {}", busAddress)
                                                             ]
                                                             consumer.handler(handler)

                                                             return handler
                                                         ])

        // add tenant to qualifier mapping to handler for but address
        LOGGER.info("vertx async event42 handler {} added with qualifier {} for tenant {}",
                       subscriber.class.simpleName,
                       qualifier,
                       tenantRef
                   )
        handler.qualifierByTenantRef.put(tenantRef, qualifier)
    }

    def private static void executeEvent(String qualifier, AuthenticationJwt authenticationJwt, EventParameters eventParams, Long invokingProcessRef) {
        val rq = new ProcessEventRequest => [
            eventHandlerQualifier   = qualifier
            eventData               = eventParams
        ]
        val srq = new ServiceRequest => [
            requestParameters       = rq
            authentication          = authenticationJwt
        ]
        if (invokingProcessRef !== null) {
            val hdr                 = new ServiceRequestHeader
            hdr.invokingProcessRef  = invokingProcessRef
            srq.requestHeader       = hdr
        }
        myVertx.runInWorkerThread(srq)
    }

    /** Register an IEventHandler as subscriber for an eventID. */
    override registerSubscriber(String eventID, Long tenantRef, IEventHandler subscriber) {

        LOGGER.debug("Registering subscriber {} for event {} in tenant {} ...", subscriber, eventID, tenantRef);

        var isNewSubscriber = true

        val key = new Pair(eventID, tenantRef)
        var currentEventHandler = SUBSCRIBERS.get(key)

        if (currentEventHandler === null) {
            var newSubscriberSet = new HashSet()
            newSubscriberSet.add(subscriber)
            SUBSCRIBERS.put(key, newSubscriberSet)
        } else if (!currentEventHandler.contains(subscriber)) {
            currentEventHandler.add(subscriber)
            SUBSCRIBERS.put(key, currentEventHandler)
        } else {
            isNewSubscriber = false
            LOGGER.info("Subscriber {} already registered for event {} in tenant {}. Skip this one.", subscriber, eventID, tenantRef)
        }

        if (isNewSubscriber && bus !== null) {
            addConsumer(eventID, tenantRef, subscriber)
        }
    }

    /** Registers an implementation of an event handler for a given ID. */
    override registerSubscriber(String eventID, IEventHandler subscriber) {
        registerSubscriber(eventID, T9tConstants.GLOBAL_TENANT_REF42, subscriber)
    }

    def static runInWorkerThread(Vertx vertx, ServiceRequest msgBody) {
        if (asyncExecutorPool !== null && Boolean.TRUE != msgBody.requestHeader?.priorityRequest) {
            asyncExecutorPool.executeBlocking([
                complete(serviceRequestExecutor.executeTrusted(msgBody))
            ], false, [
                if (succeeded) {
                    if (!ApplicationException.isOk(result.returnCode)) {
                        LOGGER.error("Async request {} FAILED with return code {}, {} ({})", msgBody.ret$PQON,
                            result.returnCode, result.errorDetails, result.errorMessage)
                    }
                } else {
                    LOGGER.error("Async request {} FAILED (worker thread terminated abnormally))", msgBody.ret$PQON)
                }
            ])
        } else {
            // no separate pool configured, or priority request
            vertx.<ServiceResponse>executeBlocking([
                complete(serviceRequestExecutor.executeTrusted(msgBody))
            ], false, [
                if (succeeded) {
                    if (!ApplicationException.isOk(result.returnCode)) {
                        LOGGER.error("Async request {} FAILED with return code {}, {} ({})", msgBody.ret$PQON,
                            result.returnCode, result.errorDetails, result.errorMessage)
                    }
                } else {
                    LOGGER.error("Async request {} FAILED (worker thread terminated abnormally))", msgBody.ret$PQON)
                }
            ])
        }
    }

    def static register(Vertx vertx) {
        myVertx = vertx
        bus = vertx.eventBus
        bus.registerCodec(new CompactMessageCodec)
        serviceRequestExecutor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor)

        val asyncPoolSize = ConfigProvider.configuration.applicationConfiguration?.localAsyncPoolSize
        if (asyncPoolSize === null) {
            LOGGER.info("Sharing executor pool with sync requests")
        } else {
            LOGGER.info("Using separate executor pool of {} threads for async requests", asyncPoolSize)
            asyncExecutorPool = vertx.createSharedWorkerExecutor("t9t-async-worker", asyncPoolSize);
        }

        val consumer = bus.consumer(AsyncProcessor.ASYNC_EVENTBUS_ADDRESS)
        consumer.completionHandler [
            if (succeeded)
                LOGGER.info("vertx async request processor successfully registered on eventbus address {}",
                    ASYNC_EVENTBUS_ADDRESS)
            else
                LOGGER.error("vertx async request processor FAILED")
        ]
        consumer.handler [
            val msgBody = body
            if (msgBody instanceof ServiceRequest) {
                LOGGER.debug("Processing an async request {}", msgBody.ret$PQON)
                vertx.runInWorkerThread(msgBody)
            } else {
                LOGGER.error("Received an async message of type {}, cannot handle!", msgBody.class.canonicalName)
            }
        ]

        // now also register any preregistered event handlers
        for (q : SUBSCRIBERS.entrySet) {
            for (IEventHandler eh : q.value) {
                addConsumer(q.key.key, q.key.value, eh)
            }
        }
    }

    // set a long enough timeout (30 minutes) to allow for concurrent batches
    val ASYNC_EVENTBUS_DELIVERY_OPTIONS = (new DeliveryOptions).addHeader("publish", "true").setSendTimeout(30 * 60 *
        1000L).setCodecName(CompactMessageCodec.COMPACT_MESSAGE_CODEC_ID)
    val PUBLISH_EVENTBUS_DELIVERY_OPTIONS = (new DeliveryOptions).addHeader("publish", "true").setSendTimeout(30 * 60 *
        1000L)

    override submitTask(ServiceRequest request) {
        LOGGER.debug("async request {} submitted via vert.x EventBus", request.requestParameters.ret$PQON)
        request.freeze // async must freeze it to avoid subsequent modification
        if (bus !== null)
            bus.send(ASYNC_EVENTBUS_ADDRESS, request, ASYNC_EVENTBUS_DELIVERY_OPTIONS)
    }

    /** Sends event data to a single subscriber. */
    override send(EventData data) {
        LOGGER.debug("async event {} sent via vert.x EventBus", data.data.ret$PQON)
//        data.freeze
        if (bus !== null) {
            val attribs = data.data
            if (attribs instanceof GenericEvent) {
                val payload = new JsonObject();
                payload.put("eventID", attribs.eventID)
                payload.put("header", new JsonObject(MapComposer.marshal(data.header)))
                if (attribs.z !== null) payload.put("z", new JsonObject(attribs.z))
                bus.send(attribs.eventID.toBusAddress, payload.toString)
            } else {
                bus.send(attribs.ret$PQON.toBusAddress, JsonComposer.toJsonString(data), PUBLISH_EVENTBUS_DELIVERY_OPTIONS) // using json for publishing eventData
            }
        } else {
            LOGGER.error("event bus is null - discarding event {}", data.data.ret$PQON)
        }
    }

    /** Publishes event data to all subscribers. */
    override publish(EventData data) {
        LOGGER.debug("async event {} published via vert.x EventBus", data.data.ret$PQON)
//        data.freeze
        if (bus !== null) {
            val attribs = data.data
            if (attribs instanceof GenericEvent) {
                val payload = new JsonObject();
                payload.put("eventID", attribs.eventID)
                payload.put("header", new JsonObject(MapComposer.marshal(data.header)))
                if (attribs.z !== null) payload.put("z", new JsonObject(attribs.z))
                bus.publish(attribs.eventID.toBusAddress, payload.toString)
            } else {
                bus.publish(attribs.ret$PQON.toBusAddress, JsonComposer.toJsonString(data), PUBLISH_EVENTBUS_DELIVERY_OPTIONS) // using json for publishing eventData
            }
        } else {
            LOGGER.error("event bus is null - discarding event {}", data.data.ret$PQON)
        }
    }
}
