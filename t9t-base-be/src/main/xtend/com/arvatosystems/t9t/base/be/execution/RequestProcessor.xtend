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

import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.T9tResponses
import com.arvatosystems.t9t.base.api.ContextlessRequestParameters
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceRequestHeader
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.auth.PermissionType
import com.arvatosystems.t9t.base.be.impl.DefaultRequestHandlerResolver
import com.arvatosystems.t9t.base.services.IBucketWriter
import com.arvatosystems.t9t.base.services.ICustomization
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.IRefGenerator
import com.arvatosystems.t9t.base.services.IRequestHandler
import com.arvatosystems.t9t.base.services.IRequestHandlerResolver
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.cfg.be.StatusProvider
import com.arvatosystems.t9t.server.ExecutionSummary
import com.arvatosystems.t9t.server.InternalHeaderParameters
import com.arvatosystems.t9t.server.services.IAuthorize
import com.arvatosystems.t9t.server.services.IRequestLogger
import com.arvatosystems.t9t.server.services.IRequestProcessor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.ObjectValidationException
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.util.ApplicationException
import de.jpaw.util.ExceptionUtil
import java.util.Objects
import org.joda.time.Instant
import org.slf4j.MDC

// process requests once the user has been authenticated
@Singleton
@AddLogger
class RequestProcessor implements IRequestProcessor {
    public static final int RTTI_MESSAGE_LOG = 2;       // defined separately because we cannot reference that class due to module isolation

    @Inject protected IRefGenerator             refGenerator
    @Inject protected IExecutor                 executor
    @Inject protected IRequestLogger            messageLogger
    @Inject protected IBucketWriter             bucketWriter
    @Inject protected IAuthorize                authorizator
    @Inject protected ICustomization            customizationProvider
    @Inject protected RequestContextScope       ctxScope    // should be the same as the previous
    protected final IRequestHandlerResolver defaultRequestHandlerResolver = new DefaultRequestHandlerResolver();  // an implementation which is independent of customization

    /** Common entry point for all executions - web service calls as well as scheduled tasks (via IUnauthenticatedServiceRequestExecutor). */
    override ServiceResponse execute(ServiceRequestHeader optHdr, RequestParameters rp, JwtInfo jwtInfo, String encodedJwt, boolean skipAuthorization) {

        // check permissions - first step
        val pqon                    = rp.ret$PQON
        val now                     = new Instant
        val millis                  = now.millis

        // validate the request header, if it exists
        if (optHdr !== null) {
            try {
                optHdr.validate
            } catch (ObjectValidationException e) {
                return MessagingUtil.createError(e, jwtInfo.tenantId, optHdr.messageId, null)
            }
        }

        val oldMdcRequestPqon = MDC.get(T9tConstants.MDC_REQUEST_PQON)
        try {
            MDC.put(T9tConstants.MDC_REQUEST_PQON, pqon)
            MDC.put(T9tConstants.MDC_TENANT_ID, jwtInfo.tenantId)
            MDC.put(T9tConstants.MDC_USER_ID, jwtInfo.userId)
            MDC.put(T9tConstants.MDC_SESSION_REF, Objects.toString(jwtInfo.sessionRef, null))
            MDC.put(T9tConstants.MDC_PROCESS_REF, null)

            // check if we are just shutting down - only for external requests
            if (!skipAuthorization && StatusProvider.isShutdownInProgress) {
                LOGGER.info("Denying processing of {}@{}:{}, shutdown is in progress", jwtInfo.userId, jwtInfo.tenantId, pqon)
                return new ServiceResponse() => [
                    returnCode          = T9tException.SHUTDOWN_IN_PROGRESS
                    tenantId            = jwtInfo.tenantId
                    errorMessage        = ApplicationException.codeToString(returnCode)
                ]
            }

            // check if JWT is still valid
            if (jwtInfo.expiresAt !== null) {
                val expiredBy = millis - jwtInfo.expiresAt.millis
                if (expiredBy > 100L) {  // allow some millis to avoid race conditions
                    LOGGER.info("Denying processing of {}@{}:{}, JWT has expired {} ms ago",
                    jwtInfo.userId, jwtInfo.tenantId, pqon, expiredBy)
                    return new ServiceResponse() => [
                        returnCode          = T9tException.JWT_EXPIRED
                        tenantId            = jwtInfo.tenantId
                        errorDetails        = Long.toString(expiredBy)
                        errorMessage        = ApplicationException.codeToString(returnCode)
                    ]
                }
            }
            if (jwtInfo.userId === null || jwtInfo.userRef === null || jwtInfo.tenantId === null || jwtInfo.tenantRef === null || jwtInfo.sessionId === null || jwtInfo.sessionRef === null) {
                LOGGER.info("Denying processing of {}@{}:{}, JWT is missing some fields", jwtInfo.userId, jwtInfo.tenantId, pqon)
                return new ServiceResponse() => [
                    returnCode          = T9tException.JWT_INCOMPLETE
                    tenantId            = jwtInfo.tenantId
                    errorMessage        = ApplicationException.codeToString(returnCode)
                ]
            }

            // 1. create a request context
            val ihdr                    = new InternalHeaderParameters
            ihdr.executionStartedAt     = now
            ihdr.encodedJwt             = encodedJwt
            ihdr.jwtInfo                = jwtInfo
            ihdr.processRef             = refGenerator.generateRef(RTTI_MESSAGE_LOG)
            ihdr.languageCode           = jwtInfo.locale
            ihdr.requestParameterPqon   = pqon
            ihdr.requestHeader          = optHdr
            ihdr.priorityRequest        = optHdr?.priorityRequest
            if (optHdr?.languageCode !== null)
                ihdr.languageCode   = optHdr.languageCode
            ihdr.freeze

            MDC.put(T9tConstants.MDC_PROCESS_REF, Objects.toString(ihdr.processRef, null))

            val prioText = if (Boolean.TRUE == ihdr.priorityRequest) "priority" else "regular"
            LOGGER.info("Starting {} request {}@{}:{}, S/R = {}/{}, need authorization={}",
            prioText, jwtInfo.userId, jwtInfo.tenantId, pqon, jwtInfo.sessionRef, ihdr.processRef, !skipAuthorization)

            val resp                    = executeSynchronous(rp, ihdr, skipAuthorization)
            val endOfProcessing         = new Instant
            val processingDuration      = endOfProcessing.millis - ihdr.executionStartedAt.millis
            resp.tenantId               = jwtInfo.tenantId
            resp.processRef             = ihdr.processRef
            resp.messageId              = optHdr?.messageId

            val logLevel = if (ApplicationException.isOk(resp.returnCode))
                jwtInfo.logLevel ?: UserLogLevelType.MESSAGE_ENTRY
            else
                jwtInfo.logLevelErrors ?: jwtInfo.logLevel ?: UserLogLevelType.REQUESTS

            val logged = logLevel.ordinal >= UserLogLevelType.MESSAGE_ENTRY.ordinal
            val returnCodeAsString = if (ApplicationException.isOk(resp.returnCode)) "OK" else ApplicationException.codeToString(resp.returnCode)

            LOGGER.info("Finished {} request {}@{}:{} with return code {} ({}) in {} ms {}, S/R = {}/{} ({})",
                prioText, jwtInfo.userId, jwtInfo.tenantId, pqon, resp.returnCode,
                resp.errorDetails ?: "",
                processingDuration, if (logged) "(LOGGED)" else "(unlogged)",
                jwtInfo.sessionRef, ihdr.processRef, returnCodeAsString
            )

            if (logged) {
                // prepare the message for transition
                val summary = new ExecutionSummary => [
                    processingTimeInMillisecs   = processingDuration
                    returnCode                  = resp.returnCode
                    errorDetails                = resp.errorDetails
                ]
                messageLogger.logRequest(ihdr, summary,
                    if (logLevel.ordinal >= UserLogLevelType.REQUESTS.ordinal) rp else null,
                    if (logLevel.ordinal >= UserLogLevelType.FULL.ordinal) resp else null
                )
            }
            return resp
        } finally {
            MDC.put(T9tConstants.MDC_REQUEST_PQON, oldMdcRequestPqon)
        }
    }

    // migrated from IExecutor, because the only callers are in this class
        /** Executes a request in a new request context. This is the only method which creates a new context.
     * All external or asynchronous requests have to pass this method, subsequent synchronous executions use different entries.
     * In addition a final boolean indicates whether authorization is required, because it is the initial call from some external source.
     */
    def protected ServiceResponse executeSynchronous(RequestParameters rq, InternalHeaderParameters ihdr, boolean skipAuthorization) {
        try {
            // we do real request processing here.
            // first, set up the context.
            val ctx = new RequestContext(ihdr, customizationProvider)

            // for special requests (macros) the request context is not injected into the thread
            if (rq instanceof ContextlessRequestParameters) {
                try {
                    val handler = defaultRequestHandlerResolver.getHandlerInstance(rq.class) as IRequestHandler<ContextlessRequestParameters>
                    return handler.execute(ctx, rq)
                } finally {
                    ctx.close
                }
                // unreachable...
            }

            // regular processing continues here
            // make the context known to the engine...
            // set the thread local to the context
            ctxScope.set(ctx)
            try {
                if (!skipAuthorization) {
                    try {
                        // request authorization - we are now within request context and can query the DB!
                        val permissions = authorizator.getPermissions(ihdr.jwtInfo, PermissionType.BACKEND, rq.ret$PQON)
                        LOGGER.debug("Backend execution permissions checked for request {}, got {}", rq.ret$PQON, permissions)
                        if (!permissions.contains(OperationType.EXECUTE)) {
                            return new ServiceResponse => [
                                returnCode          = T9tException.NOT_AUTHORIZED
                                errorDetails        = OperationType.EXECUTE.name + " on " + rq.ret$PQON
                                tenantId            = ihdr.jwtInfo.tenantId
                                processRef          = ihdr.processRef
                            ]
                        }
                        val handler                 = ctx.customization.getRequestHandler(rq)
                        val requiredPermission      = handler.getAdditionalRequiredPermission(rq)
                        if (requiredPermission !== null) {
                            if (!permissions.contains(requiredPermission)) {
                                LOGGER.debug("Failed to obtain additional permission {}", requiredPermission)
                                return new ServiceResponse => [
                                    returnCode          = T9tException.NOT_AUTHORIZED
                                    errorDetails        = requiredPermission.name + " for " + rq.ret$PQON
                                    tenantId            = ihdr.jwtInfo.tenantId
                                    processRef          = ihdr.processRef
                                ]
                            } else {
                                LOGGER.debug("Also obtained additional permission {}", requiredPermission)
                            }
                        }
                    } catch (Exception e) {
                        // any exception during permission check must lead to rejection for security reasons. We also want a full stack trace here
                        LOGGER.error("Exception during permission check: {}", e)
                        return new ServiceResponse => [
                            returnCode          = T9tException.NOT_AUTHORIZED
                            errorDetails        = e.message
                            tenantId            = ihdr.jwtInfo.tenantId
                            processRef          = ihdr.processRef
                        ]
                    }
                }
                var ServiceResponse resp = executor.executeSynchronous(ctx, rq) ?: new ServiceResponse(T9tException.REQUEST_HANDLER_RETURNED_NULL)
                if (resp.returnCode > T9tConstants.MAX_OK_RETURN_CODE && resp.errorMessage === null)
                    resp.errorMessage = MessagingUtil.truncErrorMessage(T9tException.codeToString(resp.returnCode)) // Make sure to attach necessary reference information to the outgoing response
                ctx.fillResponseStandardFields(resp) // validate the response
                try {
                    resp.validate()
                } catch (ObjectValidationException e) {
                    // log the offending request, plus the process ref (for better DB research later), and the validation error message
                    // more info
                    LOGGER.error("Response validation problem for tenantId {} and response object {}: {}", ctx.tenantId, resp.ret$PQON(), e.message)
                    LOGGER.error('''Full response is «resp»''')
                    resp = T9tResponses.createServiceResponse(T9tException.RESPONSE_VALIDATION_ERROR,
                        '''«rq.ret$PQON()»(«ctx.internalHeaderParameters.processRef»): «e.message»''')
                }
                // close the context and clean up resources
                if (ApplicationException.isOk(resp.returnCode)) {
                    try {
                        ctx.commit
                        ctx.applyPostCommitActions(rq, resp)
                        // also apply any bucket writes...
                        ctx.postBucketEntriesToQueue(bucketWriter)
                    } catch (Exception e) {
                        // commit exception: some constraint will be violated, we urgently need the cause in the log for analysis. Descend exception list...
                        val causeChain = ExceptionUtil.causeChain(e)
                        LOGGER.error("Commit failed: {}", causeChain)
                        if (e instanceof NullPointerException)
                            LOGGER.error("NPE Stack trace is ", e)
                        ctx.discardPostCommitActions
                        resp = MessagingUtil.createServiceResponse(T9tException.JTA_EXCEPTION, causeChain, null, null)
                        ctx.applyPostFailureActions(rq, resp)
                    }
                } else {
                    ctx.rollback
                    ctx.discardPostCommitActions
                }
                return resp
            } catch (Exception e) {
                val causeChain = ExceptionUtil.causeChain(e)
                LOGGER.error("Unhandled exception: {}", causeChain)
                if (e instanceof NullPointerException)
                    LOGGER.error("NPE Stack trace is ", e)
                ctx.rollback
                ctx.discardPostCommitActions
                val resp = MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null)
                ctx.applyPostFailureActions(rq, resp)
                return resp
            } finally {
                if (Thread.interrupted()) {
                    // clears the interrupted flag!
                    LOGGER.warn("Thread has been interrupted for process ref {}", ctx.requestRef)
                }
                ctx.releaseAllLocks
                ctxScope.close
                ctx.close
            }
        } catch (Exception ee) {
            val causeChain = ExceptionUtil.causeChain(ee)
            LOGGER.error("Unhandled exception (outer scope): {}", causeChain)
            if (ee instanceof NullPointerException)
                LOGGER.error("NPE Stack trace is ", ee)
            return MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, null, null)
        }
    }

    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type. (New context variant)
     * This method is called for the login process.
     */
    override <T extends ServiceResponse> T executeSynchronousAndCheckResult(RequestParameters params, InternalHeaderParameters ihdr, Class<T> requiredType, boolean skipAuthorization) {
        val oldMdcRequestPqon = MDC.get(T9tConstants.MDC_REQUEST_PQON)
        try {
            MDC.put(T9tConstants.MDC_REQUEST_PQON, params.ret$PQON)
            MDC.put(T9tConstants.MDC_TENANT_ID, ihdr.jwtInfo.tenantId)
            MDC.put(T9tConstants.MDC_USER_ID, ihdr.jwtInfo.userId)
            MDC.put(T9tConstants.MDC_SESSION_REF, Objects.toString(ihdr.jwtInfo.sessionRef, null))
            MDC.put(T9tConstants.MDC_PROCESS_REF, Objects.toString(ihdr.processRef, null))

            var ServiceResponse response = executeSynchronous(params, ihdr, skipAuthorization)
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
        } finally {
            MDC.put(T9tConstants.MDC_REQUEST_PQON, oldMdcRequestPqon)
        }
    }
}
