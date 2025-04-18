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
package com.arvatosystems.t9t.bpmn2.be.camunda.jobExecutor;

import static org.apache.commons.lang3.StringUtils.join;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.bpmn2.camunda.request.JobExecutorWorkerRequest;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.IRequestProcessor;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Execution wrapper, which is called by the {@link T9tExecuteJobsRunnable} to execute jobs for a specified tenant ref
 * in a T9T request context.
 *
 * <b>Since this implementation is called from within the {@link T9tExecuteJobsRunnable}, it will be called from
 * different worker threads and thus must be thread safe!</b> Furthermore, execution must not be performed async, to
 * ensure execution order as defined by BPMN engine!
 *
 * @author TWEL006
 */
@Singleton
public class JobExecutionRequestWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobExecutionRequestWrapper.class);

    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);
    private final IAuthenticate auth = Jdp.getRequired(IAuthenticate.class);

    private final Map<String, UUID> apiKeyPerTenantId = new ConcurrentHashMap<>();
    private UUID defaultApiKey;

    private Map<UUID, Authentication> authenticationPerApiKey = new HashMap<>();

    /**
     * Execute given job ids using a proper T9T request context of the given tenant ref. If also given an API key,
     * authentication will be done using given API key - otherwise an API key will be determined based on given tenant
     * ref.
     *
     * @param jobId
     *            Job id to execute
     * @param executionId
     *            BPMN execution the job belongs to
     * @param workflowTypeString
     *            Workflow type the job belongs to
     * @param tenantId
     *            Tenant for request context
     * @param apiKey
     *            Optional API key to perform authentication
     *
     * @return TRUE, if the job execution request was successful.
     */
    protected boolean executeJob(String jobId, String executionId, String workflowTypeString, String tenantId, UUID apiKey) {
        LOGGER.debug("Prepare job execution request for job {} of tenant {}", jobId, tenantId);

        Authentication authentication = getAuthenticationForTenant(tenantId, apiKey);
        ServiceResponse response = performJobExecutorRequest(jobId, executionId, workflowTypeString, authentication);

        if (response.getReturnCode() == T9tException.JWT_EXPIRED) {
            LOGGER.debug("Cached JWT for tenant {} expired - renew authentication and retry", tenantId);

            authentication = getAuthenticationForTenant(tenantId, apiKey);
            response = performJobExecutorRequest(jobId, executionId, workflowTypeString, authentication);
        }

        if (response.getReturnCode() != 0) {
            LOGGER.debug("Job execution for tenant ref {} failed: {} - {}", tenantId, response.getReturnCode(), response.getErrorMessage());
            return false;
        }

        return true;
    }

    /**
     * Renew cached authentication for given tenant ref.
     *
     * @param tenantId
     *            Tenant ID to get authentication for
     * @param apiKey
     *            API key to use or NULL to determine by given tenant
     */
    private synchronized Authentication renewAuthenticationForTenant(String tenantId, UUID apiKey) {
        authenticationPerApiKey.remove(apiKey);

        return getAuthenticationForTenant(tenantId, apiKey);
    }

    /**
     * Get cached authentication for given tenant ref. If none is already available or it is definitively expired, a new
     * authentication will be performed.
     *
     * @param tenantId
     *            Tenant ID to get authentication for
     * @param apiKey
     *            API key to use or NULL to determine by given tenant
     * @return Authentication (never NULL)
     */
    private synchronized Authentication getAuthenticationForTenant(String tenantId, UUID apiKey) {
        if (apiKey == null) {
            apiKey = apiKeyPerTenantId.getOrDefault(tenantId, defaultApiKey);
        }

        if (apiKey == null) {
            throw new RuntimeException("No API key available for tenant " + tenantId);
        }

        Authentication authentication = authenticationPerApiKey.get(apiKey);

        if (authentication == null || authentication.isExpired()) {
            LOGGER.debug("No existing or invalid JWT for tenant {} found - try to reauthenticate using API key", tenantId);

            authentication = performAuthentication(apiKey);
            authenticationPerApiKey.put(apiKey, authentication);
        }

        if (!tenantId.equals(authentication.jwtInfo.getTenantId())) {
            throw new RuntimeException(join("Jobs are determined to be executed in tenant ", tenantId,
                                            " but actual tenant ref available with API key authentication was ", authentication.jwtInfo.getTenantId()));
        }

        return authentication;
    }

    /**
     * Perform authentication using given API key.
     *
     * @param apiKey
     *            API key to use for authentication
     * @return Authentication data for caching
     */
    private synchronized Authentication performAuthentication(UUID apiKey) {
        final AuthenticationResponse authResponse = auth.login(new AuthenticationRequest(new ApiKeyAuthentication(apiKey)));
        final Authentication authentication = new Authentication(authResponse.getJwtInfo(), authResponse.getEncodedJwt());

        return authentication;
    }

    /**
     * Perform job execution request using given authentication for given job ids.
     *
     * @param jobId
     *            Job id to execute
     * @param executionId
     *            BPMN execution the job belongs to
     * @param workflowTypeString
     *            Workflow type the job belongs to
     * @param authentication
     *            Authentication to use
     * @return Execution result
     */
    private ServiceResponse performJobExecutorRequest(String jobId, String executionId, String workflowTypeString, Authentication authentication) {
        final JobExecutorWorkerRequest request = new JobExecutorWorkerRequest();
        request.setJobId(jobId);
        request.setExecutionId(executionId);
        request.setWorkflowType(workflowTypeString);
        return requestProcessor.execute(null, request, authentication.jwtInfo, authentication.encodedJwt, false);
    }

    /**
     * Store authentication details for reuse.
     *
     * @author TWEL006
     */
    private static class Authentication {
        private final JwtInfo jwtInfo;
        private final String encodedJwt;

        public Authentication(JwtInfo jwtInfo, String encodedJwt) {
            this.jwtInfo = jwtInfo;
            this.encodedJwt = encodedJwt;
        }

        public boolean isExpired() {
            return jwtInfo.getExpiresAt() != null && jwtInfo.getExpiresAt()
                                                            .compareTo(Instant.now()) <= 0;
        }
    }

    public UUID getDefaultApiKey() {
        return defaultApiKey;
    }

    public void setDefaultApiKey(UUID defaultApiKey) {
        this.defaultApiKey = defaultApiKey;
    }

    public Map<String, UUID> getapiKeyPerTenantId() {
        return apiKeyPerTenantId;
    }
}
