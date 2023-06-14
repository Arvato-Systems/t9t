/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.execution;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.RandomNumberGenerators;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ContextlessRequestParameters;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.RetryAdviceType;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.DefaultRequestHandlerResolver;
import com.arvatosystems.t9t.base.services.IBackendStringSanitizerFactory;
import com.arvatosystems.t9t.base.services.IBucketWriter;
import com.arvatosystems.t9t.base.services.ICustomization;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IIdempotencyChecker;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.arvatosystems.t9t.base.services.IRequestHandlerResolver;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.cfg.be.StatusProvider;
import com.arvatosystems.t9t.server.ExecutionSummary;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IRequestLogger;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

//process requests once the user has been authenticated
@Singleton
public class RequestProcessor implements IRequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestProcessor.class);

    protected final IRefGenerator refGenerator = Jdp.getRequired(IRefGenerator.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IRequestLogger messageLogger = Jdp.getRequired(IRequestLogger.class);
    protected final IIdempotencyChecker idempotencyChecker = Jdp.getRequired(IIdempotencyChecker.class);
    protected final IBucketWriter bucketWriter = Jdp.getRequired(IBucketWriter.class);
    protected final ICustomization customizationProvider = Jdp.getRequired(ICustomization.class);
    protected final IBackendStringSanitizerFactory backendStringSanitizerFactory = Jdp.getRequired(IBackendStringSanitizerFactory.class);
    protected final RequestContextScope ctxScope = Jdp.getRequired(RequestContextScope.class); // should be the same as the previous

    // an implementation which is independent of customization
    protected final IRequestHandlerResolver defaultRequestHandlerResolver = new DefaultRequestHandlerResolver();
    protected final DataConverter<String, AlphanumericElementaryDataItem> stringSanitizer = backendStringSanitizerFactory.createStringSanitizerForBackend();

    /** Common entry point for all executions - web service calls as well as scheduled tasks (via IUnauthenticatedServiceRequestExecutor). */
    @Override
    public ServiceResponse execute(final ServiceRequestHeader optHdr, final RequestParameters rp, final JwtInfo jwtInfo, final String encodedJwt,
            final boolean skipAuthorization, final Integer partition) {
        // check permissions - first step
        final String pqon = rp.ret$PQON();
        final Instant now = Instant.now();
        final long millis = now.toEpochMilli();
        final RetryAdviceType idempotencyBehaviour = rp.getIdempotencyBehaviour() == null
                ? optHdr != null ? optHdr.getIdempotencyBehaviour() : null
                : rp.getIdempotencyBehaviour();
        final UUID messageId = rp.getMessageId() == null ? optHdr != null ? optHdr.getMessageId() : null : rp.getMessageId();
        final UUID effectiveMessageId = messageId == null ? RandomNumberGenerators.randomFastUUID() : messageId;

        // validate the request header, if it exists
        if (optHdr != null) {
            try {
                optHdr.validate();
            } catch (ObjectValidationException e) {
                return MessagingUtil.createServiceResponse(e.getErrorCode(), e.getMessage(), messageId, jwtInfo.getTenantId(), null);
            }
        }

        // first thing to do is to validate (and sanitize) the business part of the request
        if (stringSanitizer != null) {
            try {
                rp.treeWalkString(stringSanitizer, true);
            } catch (ApplicationException e) {
                return MessagingUtil.createServiceResponse(e.getErrorCode(), e.getMessage(), messageId, jwtInfo.getTenantId(), null);
            }
        }

        final String oldMdcRequestPqon = MDC.get(T9tInternalConstants.MDC_REQUEST_PQON);
        try {
            MDC.put(T9tInternalConstants.MDC_MESSAGE_ID, effectiveMessageId.toString());
            MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, pqon);
            MDC.put(T9tInternalConstants.MDC_TENANT_ID, jwtInfo.getTenantId());
            MDC.put(T9tInternalConstants.MDC_USER_ID, jwtInfo.getUserId());
            MDC.put(T9tInternalConstants.MDC_SESSION_REF, Objects.toString(jwtInfo.getSessionRef(), null));
            MDC.put(T9tInternalConstants.MDC_PROCESS_REF, null);

            // check if we are just shutting down - only for external requests
            if (!skipAuthorization && StatusProvider.isShutdownInProgress()) {
                LOGGER.info("Denying processing of {}@{}:{}, shutdown is in progress", jwtInfo.getUserId(), jwtInfo.getTenantId(), pqon);
                final ServiceResponse resp = new ServiceResponse();
                resp.setMessageId(messageId);
                resp.setTenantId(jwtInfo.getTenantId());
                resp.setReturnCode(T9tException.SHUTDOWN_IN_PROGRESS);
                resp.setErrorMessage(T9tException.MSG_SHUTDOWN_IN_PROGRESS);
                return resp;
            }

            // check if JWT is still valid
            if (jwtInfo.getExpiresAt() != null) {
                final long expiredBy = millis - jwtInfo.getExpiresAt().toEpochMilli();
                if (expiredBy > 100L) {  // allow some millis to avoid race conditions
                    LOGGER.info("Denying processing of {}@{}:{}, JWT has expired {} ms ago", jwtInfo.getUserId(), jwtInfo.getTenantId(), pqon, expiredBy);
                    final ServiceResponse resp = new ServiceResponse();
                    resp.setMessageId(messageId);
                    resp.setTenantId(jwtInfo.getTenantId());
                    resp.setReturnCode(T9tException.JWT_EXPIRED);
                    resp.setErrorMessage(T9tException.MSG_JWT_EXPIRED);
                    resp.setErrorDetails(Long.toString(expiredBy));
                    return resp;
                }
            }
            if (jwtInfo.getUserId() == null || jwtInfo.getUserRef() == null || jwtInfo.getTenantId() == null
                    || jwtInfo.getSessionId() == null || jwtInfo.getSessionRef() == null) {
                LOGGER.info("Denying processing of {}@{}:{}, JWT is missing some fields", jwtInfo.getUserId(), jwtInfo.getTenantId(), pqon);
                final ServiceResponse resp = new ServiceResponse();
                resp.setMessageId(messageId);
                resp.setTenantId(jwtInfo.getTenantId());
                resp.setReturnCode(T9tException.JWT_INCOMPLETE);
                resp.setErrorMessage(T9tException.MSG_JWT_INCOMPLETE);
                return resp;
            }

            // 0. check for resend of the request
            boolean storeResult = false;
            if (messageId != null && (idempotencyBehaviour == RetryAdviceType.NEVER_RETRY || idempotencyBehaviour == RetryAdviceType.RETRY_ON_ERROR)) {
                // a message ID has been set, and also a retry behaviour, asking to not repeat the request in all cases
                // must do a check for a prior execution of this request
                final ServiceResponse idempotenceResponse = idempotencyChecker.runIdempotencyCheck(jwtInfo.getTenantId(), messageId, idempotencyBehaviour, rp);
                if (idempotenceResponse != null) {
                    return idempotenceResponse;
                }
                storeResult = true;
            }

            // 1. create a request context
            final InternalHeaderParameters ihdr = new InternalHeaderParameters();
            ihdr.setExecutionStartedAt(now);
            ihdr.setEncodedJwt(encodedJwt);
            ihdr.setJwtInfo(jwtInfo);
            ihdr.setProcessRef(refGenerator.generateRef(T9tInternalConstants.TABLENAME_MESSAGE_LOG, T9tInternalConstants.RTTI_MESSAGE_LOG));
            ihdr.setLanguageCode(jwtInfo.getLocale());
            ihdr.setRequestParameterPqon(pqon);
            ihdr.setRequestHeader(optHdr);
            ihdr.setPriorityRequest(optHdr == null ? null : optHdr.getPriorityRequest());
            if (optHdr != null && optHdr.getLanguageCode() != null) {
                ihdr.setLanguageCode(optHdr.getLanguageCode());
            }
            ihdr.setMessageId(effectiveMessageId);
            ihdr.setIdempotencyBehaviour(idempotencyBehaviour);
            ihdr.freeze();

            final String prioText = Boolean.TRUE == ihdr.getPriorityRequest() ? "priority" : "regular";
            LOGGER.debug("Starting {} request {}@{}:{}, S/R = {}/{}, need authorization={}, messageId={}", prioText, jwtInfo.getUserId(), jwtInfo.getTenantId(),
                    pqon, jwtInfo.getSessionRef(), ihdr.getProcessRef(), !skipAuthorization, effectiveMessageId);

            final ServiceResponse resp = executeSynchronousWithRetries(rp, ihdr, skipAuthorization);
            final Instant endOfProcessing = Instant.now();
            final long processingDuration = endOfProcessing.toEpochMilli() - ihdr.getExecutionStartedAt().toEpochMilli();
            resp.setTenantId(jwtInfo.getTenantId());
            resp.setProcessRef(ihdr.getProcessRef());
            resp.setMessageId(messageId);

            final UserLogLevelType logLevel;
            if (ApplicationException.isOk(resp.getReturnCode())) {
                logLevel = jwtInfo.getLogLevel() == null ? UserLogLevelType.MESSAGE_ENTRY : jwtInfo.getLogLevel();
            } else {
                if (jwtInfo.getLogLevelErrors() != null) {
                    logLevel = jwtInfo.getLogLevelErrors();
                } else if (jwtInfo.getLogLevel() != null) {
                    logLevel = jwtInfo.getLogLevel();
                } else {
                    logLevel = UserLogLevelType.REQUESTS;
                }
            }

            final boolean logged = logLevel.ordinal() >= UserLogLevelType.MESSAGE_ENTRY.ordinal();
            final String returnCodeAsString = ApplicationException.isOk(resp.getReturnCode()) ? "OK" : ApplicationException.codeToString(resp.getReturnCode());
            LOGGER.debug("Finished {} request {}@{}:{} with return code {} ({}) in {} ms {}, S/R = {}/{} ({}), messageId={}", prioText, jwtInfo.getUserId(),
                    jwtInfo.getTenantId(), pqon, resp.getReturnCode(), resp.getErrorDetails() == null ? "" : resp.getErrorDetails(), processingDuration,
                    logged ? "(LOGGED)" : "(unlogged)", jwtInfo.getSessionRef(), ihdr.getProcessRef(), returnCodeAsString, effectiveMessageId);
            if (storeResult) {
                idempotencyChecker.storeIdempotencyResult(jwtInfo.getTenantId(), messageId, idempotencyBehaviour, rp, resp);
            }

            if (logged) {
                // prepare the message for transition
                final ExecutionSummary summary = new ExecutionSummary();
                summary.setProcessingTimeInMillisecs(processingDuration);
                summary.setReturnCode(resp.getReturnCode());
                summary.setErrorDetails(resp.getErrorDetails());
                summary.setHostname(MessagingUtil.HOSTNAME);
                summary.setPartitionUsed(partition);
                messageLogger.logRequest(ihdr, summary, logLevel.ordinal() >= UserLogLevelType.REQUESTS.ordinal() ? rp : null,
                        logLevel.ordinal() >= UserLogLevelType.FULL.ordinal() ? resp : null);
            }
            return resp;
        } finally {
            MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, oldMdcRequestPqon);
        }
    }

    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type. (New context variant)
     * This method is called for the login process.
     */
    @Override
    public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestParameters params, final InternalHeaderParameters ihdr,
            final Class<T> requiredType, final boolean skipAuthorization) {
        final String oldMdcRequestPqon = MDC.get(T9tInternalConstants.MDC_REQUEST_PQON);
        try {
            MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, params.ret$PQON());
            MDC.put(T9tInternalConstants.MDC_TENANT_ID, ihdr.getJwtInfo().getTenantId());
            MDC.put(T9tInternalConstants.MDC_USER_ID, ihdr.getJwtInfo().getUserId());
            MDC.put(T9tInternalConstants.MDC_SESSION_REF, Objects.toString(ihdr.getJwtInfo().getSessionRef(), null));
            MDC.put(T9tInternalConstants.MDC_PROCESS_REF, Objects.toString(ihdr.getProcessRef(), null));

            final ServiceResponse response = executeSynchronousWithRetries(params, ihdr, skipAuthorization);

            // the response must be a subclass of the expected one
            if (!requiredType.isAssignableFrom(response.getClass())) {
                if (!ApplicationException.isOk(response.getReturnCode())) {
                    // in case of an error, this is allowed
                    LOGGER.error("Error during request handler execution for {} (returnCode={}, errorMsg={}, errorDetails={})", params.ret$PQON(),
                            response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
                    throw new T9tException(response.getReturnCode());
                }
                LOGGER.error("Error during request handler execution for {}, expected response class {} but got {}", params.ret$PQON(),
                        requiredType.getSimpleName(), response.ret$PQON());
                throw new T9tException(T9tException.INCORRECT_RESPONSE_CLASS, requiredType.getSimpleName());
            }
            return requiredType.cast(response); // all OK
        } finally {
            MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, oldMdcRequestPqon);
        }
    }

    /** Performs the retry logic in case of optimistic locking exceptions. */
    protected ServiceResponse executeSynchronousWithRetries(final RequestParameters rq, final InternalHeaderParameters ihdr, final boolean skipAuthorization) {
        int attemptsToCommit = 3;  // number of attempts fighting optimistic locking
        // we freeze all parameters to ensure that data is not modified and we retry with different parameters
        rq.freeze();
        for (;;) {
            // temporarily create a new object with mutable main record
            final RequestParameters mutableRequestParameters = rq.ret$MutableClone(true, true);  // omit deep copy of arrays: (search request sortColumns!)
            final ServiceResponse response = executeSynchronous(mutableRequestParameters, ihdr, skipAuthorization);
            if (response.getReturnCode() != T9tException.OPTIMISTIC_LOCKING_EXCEPTION) {
                return response;
            }
            attemptsToCommit -= 1;
            if (attemptsToCommit > 0) {
                LOGGER.info("Optimistic locking exception detected - retrying ({} attempts left)", attemptsToCommit);
            } else {
                LOGGER.error("Optimistic locking exception detected -problem persists, giving up");
                return response;
            }
        }
    }

    /**
     * Executes a request in a new request context. This is the only method which creates a new context.
     * All external or asynchronous requests have to pass this method, subsequent synchronous executions use different entries.
     * In addition a final boolean indicates whether authorization is required, because it is the initial call from some external source.
     */
    @SuppressWarnings("unchecked")
    protected ServiceResponse executeSynchronous(final RequestParameters rq, final InternalHeaderParameters ihdr, final boolean skipAuthorization) {
        try {
            // we do real request processing here.
            // first, set up the context.
            final RequestContext ctx = new RequestContext(ihdr, customizationProvider);

            // for special requests (macros) the request context is not injected into the thread
            if (rq instanceof ContextlessRequestParameters cRq) {
                try {
                    final IRequestHandler<ContextlessRequestParameters> handler = (IRequestHandler<ContextlessRequestParameters>) defaultRequestHandlerResolver
                            .getHandlerInstance(rq.getClass());
                    return handler.execute(ctx, cRq);
                } finally {
                    ctx.close();
                }
                // unreachable...
            }

            // regular processing continues here
            // make the context known to the engine...
            // set the thread local to the context
            ctxScope.set(ctx);
            try {
                ServiceResponse resp = skipAuthorization ? executor.executeSynchronous(ctx, rq) : executor.executeSynchronousWithPermissionCheck(ctx, rq);
                if (resp == null) {
                    resp = new ServiceResponse(T9tException.REQUEST_HANDLER_RETURNED_NULL);
                }
                if (!ApplicationException.isOk(resp.getReturnCode()) && resp.getErrorMessage() == null) {
                    // Make sure to attach necessary reference information to the outgoing response
                    resp.setErrorMessage(MessagingUtil.truncErrorMessage(ApplicationException.codeToString(resp.getReturnCode())));
                }
                ctx.fillResponseStandardFields(resp); // validate the response
                try {
                    resp.validate();
                } catch (ObjectValidationException e) {
                    // log the offending request, plus the process ref (for better DB research later), and the validation error message more info
                    LOGGER.error("Response validation problem for tenantId {} and response object {}: error {}: {}", ctx.tenantId, resp.ret$PQON(),
                            e.getErrorCode(), e.getMessage());
                    LOGGER.error("Full response is " + resp.toString());
                    resp = MessagingUtil.createServiceResponse(T9tException.RESPONSE_VALIDATION_ERROR,
                            rq.ret$PQON() + "(" + ctx.internalHeaderParameters.getProcessRef() + "): " + e.getMessage());
                }

                // close the context and clean up resources
                if (resp.getReturnCode() <  2 * ApplicationException.CLASSIFICATION_FACTOR) {
                    // both OK and DENIED responses are technically OK and must be committed. Only
                    // PARAMETER ERRORs etc are technical exceptions which must be rolled back
                    try {
                        ctx.commit();
                        ctx.applyPostCommitActions(rq, resp);
                        // also apply any bucket writes...
                        ctx.postBucketEntriesToQueue(bucketWriter);
                    } catch (Exception e) {
                        // commit exception: some constraint will be violated, we urgently need the cause in the log for analysis. Descend exception list...
                        final String causeChain = ExceptionUtil.causeChain(e);
                        resp = MessagingUtil.createServiceResponse(T9tException.JTA_EXCEPTION, causeChain, ihdr.getMessageId(), ctx.tenantId, null);
                        if (e instanceof NullPointerException) {
                            LOGGER.error("NullPointerException: Stack trace is ", e);
                        } else if (e.getClass().getCanonicalName().equals("jakarta.persistence.RollbackException")) {
                            if (e.getCause() != null && e.getCause().getClass().getCanonicalName().equals("jakarta.persistence.OptimisticLockException")) {
                                resp.setReturnCode(T9tException.OPTIMISTIC_LOCKING_EXCEPTION);
                            }
                        }
                        LOGGER.error("Commit failed: {}", causeChain);
                        ctx.discardPostCommitActions();
                        ctx.applyPostFailureActions(rq, resp);
                    }
                } else {
                    ctx.rollback();
                    ctx.discardPostCommitActions();
                    ctx.applyPostFailureActions(rq, resp);
                }
                return resp;
            } catch (Exception e) {
                final String causeChain = ExceptionUtil.causeChain(e);
                LOGGER.error("Unhandled exception: {}", causeChain);
                if (e instanceof NullPointerException) {
                    LOGGER.error("NPE Stack trace is ", e);
                }
                ctx.rollback();
                ctx.discardPostCommitActions();
                final ServiceResponse resp = MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, ihdr.getMessageId(), ctx.tenantId,
                        null);
                ctx.applyPostFailureActions(rq, resp);
                return resp;
            } finally {
                if (Thread.interrupted()) {
                    // clears the interrupted flag!
                    LOGGER.warn("Thread has been interrupted for process ref {}", ctx.requestRef);
                }
                ctx.releaseAllLocks();
                ctxScope.close();
                ctx.close();
            }
        } catch (Exception ee) {
            final String causeChain = ExceptionUtil.causeChain(ee);
            LOGGER.error("Unhandled exception (outer scope): {}", causeChain);
            if (ee instanceof NullPointerException) {
                LOGGER.error("NPE Stack trace is ", ee);
            }
            return MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain, ihdr.getMessageId(), null, null);
        }
    }
}
