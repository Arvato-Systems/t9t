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
package com.arvatosystems.t9t.ssm.be.impl;

import java.util.List;
import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceRequest;
import com.arvatosystems.t9t.base.api.ServiceRequestHeader;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.api.TransactionOriginType;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.request.ExecuteOnAllNodesRequest;
import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor;
import com.arvatosystems.t9t.base.services.IClusterEnvironment;
import com.arvatosystems.t9t.base.services.T9tInternalConstants;
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor;
import com.arvatosystems.t9t.ssm.SchedulerConcurrencyType;
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesRequest;
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesResponse;
import com.arvatosystems.t9t.ssm.request.PerformScheduledJobWithCheckRequest;

import de.jpaw.bonaparte.core.StringBuilderParser;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

/**
 * This class implements the Quartz {@link Job} interface and therefore the method {@link Job#execute(JobExecutionContext)}.
 * <p/>
 * In the execute method the information of the {@link JobDetail} part of {@link JobExecutionContext} is evaluated.
 *
 * @see PerformScheduledJob#execute(JobExecutionContext)
 */
public class PerformScheduledJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformScheduledJob.class);

    public static final boolean FIRE_ASYNCHRONOUSLY = false;

    private final IAsyncRequestProcessor sessionFactory = Jdp.getRequired(IAsyncRequestProcessor.class);
    private final IUnauthenticatedServiceRequestExecutor inProcessExecutor = Jdp.getRequired(IUnauthenticatedServiceRequestExecutor.class);
    private final RequestContextScope requestContextScope = Jdp.getRequired(RequestContextScope.class);
    private final IClusterEnvironment clusterEnvironment = Jdp.getRequired(IClusterEnvironment.class);

    /**
     * In this method the reference to the scheduler setup entry is extracted from the job data map part
     * of the job detail object accessed through the passed context object.
     * The information is extracted using the enumeration values of {@link JobDataMap}.
     * @param  context               The context object passed to the quartz scheduler.
     * @throws JobExecutionException If the job cannot be executed by Quartz properly, an exception is thrown.
     *         The {@link JobExecutionException} always contains the real cause which is of type {@link T9tException}.
     */
    @Override
    public final void execute(final JobExecutionContext context) throws JobExecutionException {
        final JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        boolean wrapForAllNodes = false;
        int redirectToNode = -1;
        try {
            final int runOnNode = dataMap.getInt(QuartzSchedulerService.DM_RUN_ON_NODE);
            // runOnNode is -1 if the task can run on any node, 0...n for a specific node, and a big number (anything > 400) for all nodes
            if ((runOnNode != (QuartzSchedulerService.RUN_ON_ANY_NODE).intValue())) {
                if (runOnNode >= 400) {
                    wrapForAllNodes = true;
                } else {
                    // check for specific node
                    final String tenantId = dataMap.getString(QuartzSchedulerService.DM_TENANT_ID);
                    if (!clusterEnvironment.processOnThisNode(tenantId, runOnNode)) {
                        // must send the job to another node
                        redirectToNode = runOnNode;
                    }
                }
            }
        } catch (final Exception e) {
            // some of the variables are missing - old scheduler setup for single node environment
        }

        // retrieve required parameters from job context
        final String apiKey            = dataMap.getString(QuartzSchedulerService.DM_API_KEY);
        final String language          = dataMap.getString(QuartzSchedulerService.DM_LANGUAGE);
        final String serializedRequest = dataMap.getString(QuartzSchedulerService.DM_REQUEST);
        final String concurrencyTypeS  = dataMap.getString(QuartzSchedulerService.DM_CONC_TYPE);
        final Long setupRef            = Long.valueOf(dataMap.getLong(QuartzSchedulerService.DM_SETUP_REF));
        final SchedulerConcurrencyType concurrencyType = concurrencyTypeS == null
                ? SchedulerConcurrencyType.RUN_PARALLEL
                : SchedulerConcurrencyType.factory(concurrencyTypeS);

        final ServiceRequestHeader header = new ServiceRequestHeader();
        header.setLanguageCode(language);
        header.setInvokingProcessRef(setupRef);
        header.setPlannedRunDate(context.getScheduledFireTime().toInstant());
        header.freeze();

        final ApiKeyAuthentication auth = new ApiKeyAuthentication(UUID.fromString(apiKey));
        auth.freeze();

        RequestParameters requestParameters
          = StringBuilderParser.<RequestParameters>unmarshal(serializedRequest, ServiceRequest.meta$$requestParameters, RequestParameters.class);
        final String requestPQON = requestParameters.ret$PQON();

        final boolean wrapInChecker = redirectToNode >= 0 && concurrencyType != SchedulerConcurrencyType.RUN_PARALLEL;
        if (wrapInChecker) {
            // wrap into a request which checks for still running instances of the same scheduler
            final PerformScheduledJobWithCheckRequest wrapperRequest = new PerformScheduledJobWithCheckRequest();
            wrapperRequest.setSchedulerSetupRef(setupRef);
            wrapperRequest.setConcurrencyType(concurrencyType);
            wrapperRequest.setRequest(requestParameters);
            requestParameters = wrapperRequest;
        }
        if (wrapForAllNodes) {
            requestParameters = new ExecuteOnAllNodesRequest(requestParameters);
        }

        // set some header values for the outmost parameters
        requestParameters.setWhenSent(header.getPlannedRunDate().toEpochMilli());
        requestParameters.setTransactionOriginType(TransactionOriginType.SCHEDULER);
        requestParameters.setEssentialKey(setupRef.toString());

        final ServiceRequest srq = new ServiceRequest();
        srq.setRequestHeader(header);
        srq.setRequestParameters(requestParameters);
        srq.setAuthentication(auth);

        // service request fully constructed
        if (redirectToNode >= 0) {
            // instead of running it locally, send it to a remote node via kafka
            LOGGER.info("Sending scheduled request {} to node {} via kafka", requestPQON, redirectToNode);
            clusterEnvironment.processOnOtherNode(srq, redirectToNode, setupRef);
            return;
        }

        MDC.clear();
        String jobName = null;
        if (context.getJobDetail() != null) {
            final JobKey key = context.getJobDetail().getKey();
            if (key != null) {
                jobName = key.getName();
            }
        }
        MDC.put(T9tInternalConstants.MDC_SSM_JOB_ID, jobName);

        if (PerformScheduledJob.FIRE_ASYNCHRONOUSLY) {
            this.sessionFactory.submitTask(srq, true, false);
        } else {
            try {
                // check for previous instance
                // we are outside of any RequestContext, and would need to invoke separate request handlers to perform any actions
                // therefore do first tests directly
                final boolean mayStartNewInstance = this.checkPriorInstances(dataMap, setupRef, header, auth);
                if (mayStartNewInstance) {
                    final ServiceResponse resp = this.inProcessExecutor.execute(srq);
                    // perform a check if the event was successful
                    if (!ApplicationException.isOk(resp.getReturnCode())) {
                        LOGGER.error("Quartz task NOT completed successfully: {} at {} terminated with code {}: {}",
                                requestPQON, srq.getRequestHeader().getPlannedRunDate(),
                            Integer.valueOf(resp.getReturnCode()), resp.getErrorDetails());
                    }
                } else {
                    LOGGER.info("Skipping scheduled start of request {} for {}", requestPQON, srq.getRequestHeader().getPlannedRunDate());
                }
            } catch (final Exception e) {
                LOGGER.error("Problem performing scheduler analysis: {}", ExceptionUtil.causeChain(e));
            }
        }
    }

    protected boolean checkPriorInstances(final JobDataMap dataMap, final Long setupRef, final ServiceRequestHeader header, final ApiKeyAuthentication auth) {
        try {
            final String concurrencyType = dataMap.getString(QuartzSchedulerService.DM_CONC_TYPE);
            final int timeLimit = dataMap.getInt(QuartzSchedulerService.DM_TIME_LIMIT);
            if ((concurrencyType == null || concurrencyType.equals(SchedulerConcurrencyType.RUN_PARALLEL.getToken()) && timeLimit == 0)) {
                // run in parallel, no matter how old - shortcut 1: no thread analysis
                return true;
            }
        } catch (final Exception e) {
            LOGGER.warn("Missing Quartz JobDataMap entries - assuming old scheduler entries: {}", ExceptionUtil.causeChain(e));
            return true;   // data not set - Quartz crashes if a null is encountered (and unfortunately also does not offer a method to check for a valid entry)
        }
        // need analysis of prior instances
        final List<ProcessStatusDTO> priorInstances = this.requestContextScope.getProcessStatusForScheduler(setupRef);
        if (priorInstances.isEmpty()) {
            // there are no prior instances
            return true;
        }

        // there are prior instances, and we expect some action required
        LOGGER.info("Scheduler congestion: There are {} prior instances running for setup objectRef {}", setupRef);
        final ServiceRequest srq = new ServiceRequest();
        srq.setRequestHeader(header);
        srq.setRequestParameters(new DealWithPriorJobInstancesRequest(setupRef));
        srq.setAuthentication(auth);
        final ServiceResponse resp = this.inProcessExecutor.executeTrusted(srq);
        if (resp instanceof DealWithPriorJobInstancesResponse dwpjir) {
            return dwpjir.getInvokeNewInstance();
        } else {
            LOGGER.error("Bad response from DealWithPriorJobInstancesRequest: {}", resp);
            return true;
        }
    }
}
