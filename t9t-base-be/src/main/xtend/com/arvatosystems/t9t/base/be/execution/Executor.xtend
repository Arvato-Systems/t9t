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
package com.arvatosystems.t9t.base.be.execution

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.T9tResponses
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceRequestHeader
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.event.EventData
import com.arvatosystems.t9t.base.event.EventHeader
import com.arvatosystems.t9t.base.event.EventParameters
import com.arvatosystems.t9t.base.event.InvalidateCacheEvent
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.IForeignRequest
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.base.types.AuthenticationJwt
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.core.BonaPortableClass
import de.jpaw.bonaparte.core.ObjectValidationException
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import de.jpaw.dp.Singleton
import de.jpaw.util.ApplicationException
import de.jpaw.util.ExceptionUtil
import java.util.Set
import org.slf4j.MDC

/**
 * Class serving as key entry point for intra-module communication.
 * It is not intended to be changed or overwritten, the only reason why this is not static is, because
 * tests might want to mock its methods.
 */
@AddLogger
@Singleton
class Executor implements IExecutor, T9tConstants {
    @Inject protected IForeignRequest           foreignRequestExecutor
    @Inject protected IAsyncRequestProcessor    asyncProcessor
    @Inject protected Provider<RequestContext>  contextProvider

    // execute a sub-request within the same existing context
    override ServiceResponse executeSynchronous(RequestParameters params) {
        return executeSynchronous(contextProvider.get(), params)
    }

    // execute a sub-request within the same existing (and known) context
    override ServiceResponse executeSynchronous(RequestContext ctx, RequestParameters params) {
        // validate the request parameters
        try {
            params.validate()
        } catch (ObjectValidationException e) {
            // log the offending request, plus the process ref (for better DB research later), and the validation error message
            // more info
            LOGGER.error("Synchronous request validation problem for tenantId {} and parameter object type {}", ctx.tenantId, params.ret$PQON())
            LOGGER.error('''Full request parameters are «params»''')
            return T9tResponses.createServiceResponse(T9tException.REQUEST_VALIDATION_ERROR, '''«params.ret$PQON()»(«ctx.internalHeaderParameters.processRef»): «e.message»''')
        }
        var ServiceResponse response = null
        var BonaPortableClass<?> bp = params.ret$BonaPortableClass()
        // LOGGER.debug("FT-2454: isForeignCheck({}) (= {}?)", bp.pqon, params.ret$PQON)

        val oldMdcRequestPqon = MDC.get(T9tConstants.MDC_REQUEST_PQON)
        try {
            MDC.put(T9tConstants.MDC_REQUEST_PQON, bp.pqon)
            ctx.pushCallStack(params.ret$PQON);
            // check for alien requests
            if (!bp.pqon.startsWith("t9t.")) {
                // call out to external system
                // LOGGER.debug("FT-2454: foreign request, sending to wildfly")
                return foreignRequestExecutor.execute(ctx, params)
            }
            // LOGGER.debug("FT-2454: local request, doing myself!")

            val handler = ctx.customization.getRequestHandler(params)
            response = handler.execute(ctx, params) // execute the new method, possibly redirected temporarily by AbstractRequestHandler
            // verify the promise concerning the return type has been kept. As all BonaPortableClass'es are singletons, == should be fine
            if (bp.returns !== ServiceResponse.BClass.INSTANCE) {
                // we expect something different than the default... Check it in case of OK messages, as the API could be misleading otherwise!
                if (response.returnCode < ApplicationException.CLASSIFICATION_FACTOR) {
                    // the response is OK (in case of error, a shortened response is accepted)
                    if (!bp.returns.bonaPortableClass.isAssignableFrom(response.class)) {
                        LOGGER.warn("{} breaks the response type promise and returns {} instead of {} for request {}",
                            params.ret$PQON(), response.ret$PQON(), bp.returns.pqon, ctx.internalHeaderParameters.processRef)
                    }
                }

            }
            // central setting of errorMessage. Also warn if request handler is cooking some own soup here
            if (response.errorMessage !== null) {
                LOGGER.warn("Request {} / response {}: manually setting errorMessage is extremely discouraged: {}",
                    params.ret$PQON(), response.ret$PQON(), response.errorMessage)
            }
            // Finally we're done, return the response
            return response
        } catch (ApplicationException e) {
            // log a stack trace in case of 8000 or 9000 type errors
            val classification = e.classification
            if (classification == ApplicationException.CL_INTERNAL_LOGIC_ERROR || classification == ApplicationException.CL_DATABASE_ERROR) {
                // stack trace should be useful for analysis
                // provide full stack trace to the log
                LOGGER.error("Execution problem: internal logic (8xxx) or general error (9xxx): Cause is: ", e) // create a service response that reports about the problem
            }
            return T9tResponses.createServiceResponse(e.errorCode, e.message)
        } catch (Exception e) {
            // provide full stack trace to the log
            val causeChain = ExceptionUtil.causeChain(e)
            LOGGER.error("Execution problem: General error cause is: ", e) // create a service response that reports about the problem
            return T9tResponses.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain)
        } finally {
            ctx.popCallStack
            MDC.put(T9tConstants.MDC_REQUEST_PQON, oldMdcRequestPqon)
        }
    }


    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type.
     */
    override <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestParameters params, Class<T> requiredType) {
        return executeSynchronousAndCheckResult(contextProvider.get, params, requiredType);
    }

    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type.
     */
    override <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestContext ctx, RequestParameters params, Class<T> requiredType) {
        var ServiceResponse response = executeSynchronous(ctx, params)
        if ((response.returnCode > T9tConstants.MAX_OK_RETURN_CODE)) {
            LOGGER.error("Error during request handler execution for {} (returnCode={}, errorMsg={}, errorDetails={})",
                params.ret$PQON(), response.returnCode, response.errorMessage, response.errorDetails)
            throw new T9tException(response.returnCode)
        }
        // the response must be a subclass of the expected one
        if (!requiredType.isAssignableFrom(response.class)) {
            LOGGER.error("Error during request handler execution for {}, expected response class {} but got {}",
                params.ret$PQON(), requiredType.simpleName, response.ret$PQON())
            throw new T9tException(T9tException.INCORRECT_RESPONSE_CLASS, requiredType.simpleName)
        }
        return requiredType.cast(response) // all OK
    }

    /**
     * Schedules the provided request asynchronously. It will be executed with the same userId / tenant as the current request, and only if the current
     * request is technically successful (i.e. does no rollback, i.e. a returncode is 0 <= r <= 199999999).
     * This will be performed via the PostCommitHook
     */

    override void executeAsynchronous(RequestParameters params) {
        executeAsynchronous(contextProvider.get(), params, false)
    }

    override void executeAsynchronous(RequestContext ctx, RequestParameters params) {
        executeAsynchronous(contextProvider.get(), params, false)
    }

    override void executeAsynchronous(RequestContext ctx, RequestParameters params, boolean priority) {
        try {
            params.validate()
        } catch (ObjectValidationException e) {
            LOGGER.error("Asynchronous request validation problem for tenantId {} and parameter object type {}", ctx.tenantId, params.ret$PQON())
            LOGGER.error("Full request parameters are {}", params)
            throw new T9tException(T9tException.REQUEST_VALIDATION_ERROR,
                '''«params.ret$PQON()»(«ctx.internalHeaderParameters.processRef»): «e.message»'''.toString)
        }
        // create a ServiceRequest (as parameter for the async executor)
        val srq                 = new ServiceRequest
        srq.requestHeader       = new ServiceRequestHeader => [
            languageCode        = ctx.internalHeaderParameters.languageCode
            invokingProcessRef  = ctx.internalHeaderParameters.processRef
            plannedRunDate      = ctx.internalHeaderParameters.plannedRunDate
            priorityRequest     = if (priority || ctx.priorityRequest) Boolean.TRUE else ctx.internalHeaderParameters.priorityRequest
        ]
        srq.requestParameters   = params
        srq.authentication      = new AuthenticationJwt(ctx.internalHeaderParameters.encodedJwt)
        ctx.addPostCommitHook[ oldCtx, rq, rs | asyncProcessor.submitTask(srq) ]
    }


    def private toHeader(RequestContext ctx) {
        return new EventHeader => [
            tenantId            = ctx.tenantId
            tenantRef           = ctx.tenantRef
            invokingProcessRef  = ctx.internalHeaderParameters.processRef
            encodedJwt          = ctx.internalHeaderParameters.encodedJwt
        ]
    }

    override void sendEvent(EventParameters data) {
        sendEvent(contextProvider.get(), data)
    }

    override void sendEvent(RequestContext ctx, EventParameters data) {
        data.validate()
        // create an EventData structure
        val eventData           = new EventData
        eventData.header        = ctx.toHeader
        eventData.data          = data
        eventData.header.freeze
        ctx.addPostCommitHook[ asyncProcessor.send(eventData) ]
    }

    override void publishEvent(EventParameters data) {
        publishEvent(contextProvider.get(), data)
    }

    override void publishEvent(RequestContext ctx, EventParameters data) {
        data.validate()
        // create an EventData structure
        val eventData           = new EventData
        eventData.header        = ctx.toHeader
        eventData.data          = data
        eventData.header.freeze
        ctx.addPostCommitHook[ asyncProcessor.publish(eventData) ]
    }

    /** Writes to multiple buckets. */
    override writeToBuckets(Set<String> bucketIds, Long ref, Integer mode) {
        val ctx = contextProvider.get()
        for (id : bucketIds)
            ctx.writeBucket(id, ref, mode)
    }

    /** Publishes an event to clear caches.
     * cacheId usually is the DTO simple name or JPA entity base class name (or PQON),
     * key can be null, or specifies an element.
     */
    override void clearCache(String cacheId, BonaPortable key) {
        // if in cluster mode, send a cache invalidation event also to other nodes (after successful commit)
//        if (Boolean.TRUE.equals(ConfigProvider.getConfiguration().getRunInCluster())) {
            publishEvent(new InvalidateCacheEvent(cacheId, key));
//        }
    }
}
