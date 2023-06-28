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
package com.arvatosystems.t9t.base.be.execution;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.JwtAuthentication;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.event.InvalidateCacheEvent;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IAuthorize;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

/**
 * Class serving as key entry point for intra-module communication.
 * It is not intended to be changed or overwritten, the only reason why this is not static is, because
 * tests might want to mock its methods.
 */
@Singleton
public class Executor implements IExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Executor.class);

    protected final IAsyncRequestProcessor asyncProcessor = Jdp.getRequired(IAsyncRequestProcessor.class);
    protected final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);
    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);
    protected final boolean readonlyOptimization; // if set, the JPA session will be set to readonly in case the top level request handler says that's possible.

    public Executor() {
        final RelationalDatabaseConfiguration dbCfg = ConfigProvider.getConfiguration().getDatabaseConfiguration();
        readonlyOptimization = dbCfg != null && !Boolean.FALSE.equals(dbCfg.getReadonlyOptimization());
    }

    // execute a sub-request within the same existing context
    @Override
    public ServiceResponse executeSynchronous(final RequestParameters params) {
        return executeSynchronous(contextProvider.get(), params);
    }

    // execute a sub-request within the same existing (and known) context, if sufficient permissions available
    @Override
    public ServiceResponse executeSynchronousWithPermissionCheck(final RequestContext ctx, final RequestParameters params) {
        final ServiceResponse errorResp = permissionCheck(ctx, params);
        if (errorResp != null) {
            return errorResp;
        }
        return executeSynchronous(ctx, params);
    }

    @Override
    public ServiceResponse permissionCheck(final RequestContext ctx, final RequestParameters params) {
        try {
            // request authorization - we are now within request context and can query the DB!
            final Permissionset permissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND, params.ret$PQON());
            LOGGER.trace("Backend execution permissions checked for request {}, got {}", params.ret$PQON(), permissions);
            if (!permissions.contains(OperationType.EXECUTE)) {
                return errorResponse(ctx, T9tException.NOT_AUTHORIZED, OperationType.EXECUTE.name() + " on " + params.ret$PQON());
            }
            final IRequestHandler<RequestParameters> handler = ctx.customization.getRequestHandler(params);
            final OperationType requiredPermission = handler.getAdditionalRequiredPermission(params);
            if (requiredPermission != null) {
                if (!permissions.contains(requiredPermission)) {
                    LOGGER.debug("Failed to obtain additional permission {}", requiredPermission);
                    return errorResponse(ctx, T9tException.NOT_AUTHORIZED, requiredPermission.name() + " for " + params.ret$PQON());
                } else {
                    LOGGER.trace("Also obtained additional permission {}", requiredPermission);
                }
            }
        } catch (Exception e) {
            // any exception during permission check must lead to rejection for security reasons. We also want a full stack trace here
            LOGGER.error("Exception during permission check: {}", e);
            return errorResponse(ctx, T9tException.NOT_AUTHORIZED, e.getMessage());
        }
        return null;
    }

    /** Generates a populated ServiceResponse from <code>InternalHeaderParameters</code>. Used from <code>executeSynchronousWithPermissionCheck</code>. */
    protected ServiceResponse errorResponse(final RequestContext ctx, final int errorCode, final String details) {
        final ServiceResponse resp = new ServiceResponse();
        resp.setReturnCode(errorCode);
        resp.setErrorDetails(details);
        final InternalHeaderParameters ihdr = ctx.internalHeaderParameters;
        resp.setTenantId(ihdr.getJwtInfo().getTenantId());
        resp.setProcessRef(ihdr.getProcessRef());
        resp.setMessageId(ihdr.getMessageId());
        return resp;
    }

    // execute a sub-request within the same existing (and known) context
    @Override
    public ServiceResponse executeSynchronous(final RequestContext ctx, final RequestParameters params) {
        // validate the request parameters
        try {
            params.validate();
        } catch (ObjectValidationException e) {
            // log the offending request, plus the process ref (for better DB research later), and the validation error message
            // more info
            LOGGER.error("Synchronous request validation problem for tenantId {} and parameter object type {}: error {}: {}", ctx.tenantId, params.ret$PQON(),
                    e.getErrorCode(), e.getMessage());
            LOGGER.error("Full request parameters are {}", params);
            return MessagingUtil.createServiceResponse(T9tException.REQUEST_VALIDATION_ERROR,
                    params.ret$PQON() + "(" + ctx.internalHeaderParameters.getProcessRef() + ":" + e.getMessage());
        }

        ServiceResponse response = null;
        BonaPortableClass<?> bp = params.ret$BonaPortableClass();

        final String oldMdcRequestPqon = MDC.get(T9tInternalConstants.MDC_REQUEST_PQON);
        try {
            MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, bp.getPqon());
            ctx.pushCallStack(params.ret$PQON());

            final IRequestHandler<RequestParameters> handler = ctx.customization.<RequestParameters>getRequestHandler(params);
            if (ctx.isTopLevelRequest()) {
                // top level request: check for read-only session
                ctx.setReadOnlyMode(handler.isReadOnly(params));
            }
            response = handler.execute(ctx, params); // execute the new method, possibly redirected temporarily by AbstractRequestHandler
            // verify the promise concerning the return type has been kept. As all BonaPortableClass'es are singletons, == should be fine
            if (!ServiceResponse.BClass.INSTANCE.equals(bp.getReturns())) {
                // we expect something different than the default... Check it in case of OK messages, as the API could be misleading otherwise!
                if (response.getReturnCode() < ApplicationException.CLASSIFICATION_FACTOR) {
                    // the response is OK (in case of error, a shortened response is accepted)
                    if (!bp.getReturns().getBonaPortableClass().isAssignableFrom(response.getClass())) {
                        LOGGER.warn("{} breaks the response type promise and returns {} instead of {} for request {}",
                                params.ret$PQON(), response.ret$PQON(), bp.getReturns().getPqon(), ctx.internalHeaderParameters.getProcessRef());
                    }
                }
            }
            // central setting of errorMessage. Also warn if request handler is cooking some own soup here
            if (response.getErrorMessage() != null) {
                LOGGER.warn("Request {} / response {}: manually setting errorMessage is extremely discouraged: {}",
                    params.ret$PQON(), response.ret$PQON(), response.getErrorMessage());
            }
            // Finally we're done, return the response
            return response;
        } catch (final ApplicationException e) {
            // log a stack trace in case of 8000 or 9000 type errors
            final int classification = e.getClassification();
            if (classification == ApplicationException.CL_INTERNAL_LOGIC_ERROR || classification == ApplicationException.CL_DATABASE_ERROR) {
                // stack trace should be useful for analysis
                // provide full stack trace to the log
                // create a service response that reports about the problem
                LOGGER.error("Execution problem: internal logic (8xxx) or general error (9xxx): Cause is: ", e);
            }
            return MessagingUtil.createServiceResponse(e.getErrorCode(), e.getMessage());
        } catch (final Throwable e) {
            // provide full stack trace to the log
            final String causeChain = ExceptionUtil.causeChain(e);
            final boolean notThrowable = e instanceof Exception;
            LOGGER.error("Execution problem{}: General error cause is: ", notThrowable ? "" : " (THROWABLE!)");
            // create a service response that reports about the problem
            return MessagingUtil.createServiceResponse(T9tException.GENERAL_EXCEPTION, causeChain);
        } finally {
            ctx.popCallStack();
            MDC.put(T9tInternalConstants.MDC_REQUEST_PQON, oldMdcRequestPqon);
        }
    }

    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type.
     */
    @Override
    public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestParameters params, final Class<T> requiredType) {
        return executeSynchronousAndCheckResult(contextProvider.get(), params, requiredType);
    }

    /**
     * Invokes executeSynchronous() and checks the result for correctness and the response type.
     */
    @Override
    public <T extends ServiceResponse> T executeSynchronousAndCheckResult(final RequestContext ctx, final RequestParameters params,
            final Class<T> requiredType) {
        final ServiceResponse response = executeSynchronous(ctx, params);
        if (!ApplicationException.isOk(response.getReturnCode())) {
            LOGGER.error("Error during request handler execution for {} (returnCode={}, errorMsg={}, errorDetails={})", params.ret$PQON(),
                    response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
            throw new T9tException(response.getReturnCode(), response.getErrorDetails());
        }
        // the response must be a subclass of the expected one
        if (!requiredType.isAssignableFrom(response.getClass())) {
            LOGGER.error("Error during request handler execution for {}, expected response class {} but got {}", params.ret$PQON(),
                    requiredType.getSimpleName(), response.ret$PQON());
            throw new T9tException(T9tException.INCORRECT_RESPONSE_CLASS, requiredType.getSimpleName());
        }
        return requiredType.cast(response); // all OK
    }

    /**
     * Schedules the provided request asynchronously. It will be executed with the same userId / tenant as the current request, and only if the current
     * request is technically successful (i.e. does no rollback, i.e. a returncode is 0 <= r <= 199999999).
     * This will be performed via the PostCommitHook
     */
    @Override
    public void executeAsynchronous(final RequestParameters params) {
        executeAsynchronousSub(contextProvider.get(), params, false, false);
    }

    @Override
    public void executeAsynchronous(final RequestContext ctx, final RequestParameters params) {
        executeAsynchronousSub(contextProvider.get(), params, false, false);
    }

    @Override
    public void executeAsynchronous(final RequestContext ctx, final RequestParameters params, final boolean priority) {
        executeAsynchronousSub(ctx, params, priority, false);
    }

    @Override
    public void executeOnEveryNode(final RequestContext ctx, final RequestParameters params) {
        executeAsynchronousSub(ctx, params, false, true);
    }

    protected void executeAsynchronousSub(final RequestContext ctx, final RequestParameters params, final boolean priority, final boolean allNodes) {
        try {
            params.validate();
        } catch (ObjectValidationException e) {
            LOGGER.error("Asynchronous request validation problem for tenantId {} and parameter object type {}: error {}: {}", ctx.tenantId, params.ret$PQON(),
                    e.getErrorCode(), e.getMessage());
            LOGGER.error("Full request parameters are {}", params);
            throw new T9tException(T9tException.REQUEST_VALIDATION_ERROR,
                    params.ret$PQON() + "(" + ctx.internalHeaderParameters.getProcessRef() + "):" + e.getMessage());
        }
        // create a ServiceRequest (as parameter for the async executor)
        final ServiceRequest srq = new ServiceRequest();
        final ServiceRequestHeader requestHeader = new ServiceRequestHeader();
        requestHeader.setLanguageCode(ctx.internalHeaderParameters.getLanguageCode());
        requestHeader.setInvokingProcessRef(ctx.internalHeaderParameters.getProcessRef());
        requestHeader.setPlannedRunDate(ctx.internalHeaderParameters.getPlannedRunDate());
        requestHeader.setPriorityRequest((priority || ctx.isPriorityRequest()) ? Boolean.TRUE : ctx.internalHeaderParameters.getPriorityRequest());
        srq.setRequestHeader(requestHeader);
        srq.setRequestParameters(params);
        srq.setAuthentication(new JwtAuthentication(ctx.internalHeaderParameters.getEncodedJwt()));
        ctx.addPostCommitHook((final RequestContext previousRequestContext, final RequestParameters rq, final ServiceResponse rs) -> {
            asyncProcessor.submitTask(srq, true, allNodes);
        });
    }

    @Override
    public void sendEvent(final EventParameters data) {
        sendEvent(contextProvider.get(), data);
    }

    @Override
    public void sendEvent(final RequestContext ctx, final EventParameters data) {
        data.validate();
        ctx.addPostCommitHook((final RequestContext previousRequestContext, final RequestParameters rq, final ServiceResponse rs) -> {
            asyncProcessor.send(asyncProcessor.toEventData(ctx, data));
        });
    }

    @Override
    public void publishEvent(final EventParameters data) {
        publishEvent(contextProvider.get(), data);
    }

    @Override
    public void publishEvent(final RequestContext ctx, final EventParameters data) {
        data.validate();
        ctx.addPostCommitHook((final RequestContext previousRequestContext, final RequestParameters rq, final ServiceResponse rs) -> {
            asyncProcessor.send(asyncProcessor.toEventData(ctx, data));
        });
    }

    /**
     * Writes to multiple buckets.
     * */
    @Override
    public void writeToBuckets(final Set<String> bucketIds, final Long ref, final Integer mode) {
        final RequestContext ctx = contextProvider.get();
        for (final String id: bucketIds) {
            ctx.writeBucket(id, ref, mode);
        }
    }

    /**
     * Publishes an event to clear caches.
     * cacheId usually is the DTO simple name or JPA entity base class name (or PQON),
     * key can be null, or specifies an element.
     */
    @Override
    public void clearCache(final String cacheId, final BonaPortable key) {
        publishEvent(new InvalidateCacheEvent(cacheId, key));
    }

    /**
     * Publishes an event to clear caches.
     * cacheId usually is the DTO simple name or JPA entity base class name (or PQON),
     * key can be null, or specifies an element.
     */
    @Override
    public void clearCache(final RequestContext ctx, final String cacheId, final BonaPortable key) {
        publishEvent(ctx, new InvalidateCacheEvent(cacheId, key));
    }
}
