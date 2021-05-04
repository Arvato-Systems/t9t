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
package com.arvatosystems.t9t.ssm.be.impl

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.api.ServiceRequest
import com.arvatosystems.t9t.base.api.ServiceRequestHeader
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication
import com.arvatosystems.t9t.base.be.execution.RequestContextScope
import com.arvatosystems.t9t.base.services.IAsyncRequestProcessor
import com.arvatosystems.t9t.server.services.IUnauthenticatedServiceRequestExecutor
import com.arvatosystems.t9t.ssm.SchedulerConcurrencyType
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesRequest
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.StringBuilderParser
import de.jpaw.dp.Inject
import de.jpaw.util.ApplicationException
import de.jpaw.util.ExceptionUtil
import java.util.UUID
import org.joda.time.Instant
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.slf4j.MDC
import com.arvatosystems.t9t.base.services.IClusterEnvironment

/**
 * This class implements the Quartz {@link Job} interface and therefore the method {@link Job#execute(JobExecutionContext)}.
 * <p/>
 * In the execute method the information of the {@link JobDetail} part of {@link JobExecutionContext} is evaluated.
 * @see PerformScheduledJob#execute(JobExecutionContext)
 */
@AddLogger
class PerformScheduledJob implements Job {
    public static final boolean FIRE_ASYNCHRONOUSLY = false;  // if true, jobs are submitted via asynchronous event, otherwise executed in process
    @Inject IAsyncRequestProcessor sessionFactory
    @Inject IUnauthenticatedServiceRequestExecutor inProcessExecutor
    @Inject RequestContextScope requestContextScope
    @Inject IClusterEnvironment clusterEnvironment

    /**
     * In this method the reference to the scheduler setup entry is extracted from the job data map part of the job detail object accessed through the passed context object.
     * The information is extracted using the enumeration values of {@link JobDataMap}.
     * @param context The context object passed to the quartz scheduler.
     * @throws JobExecutionException If the job cannot be executed by Quartz properly, an exception is thrown. The {@link JobExecutionException} always contains the real cause which is of type {@link T9tException}
     */
    override final void execute(JobExecutionContext context) throws JobExecutionException {
        val dataMap             = context.jobDetail.jobDataMap

        try {
            val runOnNode       = dataMap.getInt   (QuartzSchedulerService.DM_RUN_ON_NODE);
            if (runOnNode != QuartzSchedulerService.RUN_ON_ALL_NODES) {
                // this task should be run on a specific node only, check if that's us
                val tenantRef   = dataMap.getLong  (QuartzSchedulerService.DM_TENANT_REF);
                if (!clusterEnvironment.processOnThisNode(tenantRef, runOnNode)) {
                    return;  // not suitable for this node
                }
            }
        } catch (Exception e) {
            // some of the variables are missing - old scheduler setup for single node environment
        }

        val apiKey              = dataMap.getString(QuartzSchedulerService.DM_API_KEY);
        val language            = dataMap.getString(QuartzSchedulerService.DM_LANGUAGE);
        val serializedRequest   = dataMap.getString(QuartzSchedulerService.DM_REQUEST);
        val setupRef            = dataMap.getLong  (QuartzSchedulerService.DM_SETUP_REF);

        val requestParameters   = StringBuilderParser.unmarshal(serializedRequest, ServiceRequest.meta$$requestParameters, RequestParameters)
        val header              = new ServiceRequestHeader => [
            languageCode        = language
            invokingProcessRef  = setupRef
            plannedRunDate      = new Instant(context.scheduledFireTime)
            freeze
        ]
        val auth                = new ApiKeyAuthentication(UUID.fromString(apiKey))
        auth.freeze
        val srq                 = new ServiceRequest
        srq.requestHeader       = header
        srq.requestParameters   = requestParameters
        srq.authentication      = auth

        MDC.clear
        MDC.put(T9tConstants.MDC_SSM_JOB_ID, context.getJobDetail?.getKey?.getName)
        if (FIRE_ASYNCHRONOUSLY) {
            sessionFactory.submitTask(srq)
        } else {
            try {
                // check for previous instance
                // we are outside of any RequestContext, and would need to invoke separate request handlers to perform any actions - therefore do first tests directly
                val mayStartNewInstance = checkPriorInstances(dataMap, setupRef, header, auth)
                if (mayStartNewInstance) {
                    val resp = inProcessExecutor.execute(srq)
                    // perform a check if the event was successful
                    if (!ApplicationException.isOk(resp.returnCode))
                        LOGGER.error("Quartz task NOT completed successfully: {} at {} terminated with code {}: {}",
                            requestParameters.ret$PQON, srq.requestHeader.plannedRunDate, resp.returnCode, resp.errorDetails
                        )
                } else {
                    LOGGER.info("Skipping scheduled start of request {} for {}", requestParameters.ret$PQON, srq.requestHeader.plannedRunDate)
                }
            } catch (Exception e) {
                LOGGER.error("Problem performing scheduler analysis: {}", ExceptionUtil.causeChain(e))
            }
        }
    }

    def protected boolean checkPriorInstances(JobDataMap dataMap, Long setupRef, ServiceRequestHeader header, ApiKeyAuthentication auth) {
        try {
            val concurrencyType     = dataMap.getString(QuartzSchedulerService.DM_CONC_TYPE);
            val timeLimit           = dataMap.getInt   (QuartzSchedulerService.DM_TIME_LIMIT);
            if ((concurrencyType === null || concurrencyType == SchedulerConcurrencyType.RUN_PARALLEL.token) && timeLimit == 0) {
                // run in parallel, no matter how old - shortcut 1: no thread analysis
                 return true;
            }
        } catch (Exception e) {
            LOGGER.warn("Missing Quartz JobDataMap entries - assuming old scheduler entries: {}", ExceptionUtil.causeChain(e));
            return true;   // data not set - Quartz crashes if a null is encountered (and unfortunately also does not offer a method to check for a valid entry)
        }
        // need analysis of prior instances
        val priorInstances = requestContextScope.getProcessStatusForScheduler(setupRef)
        if (priorInstances.isEmpty) {
            // there are no prior instances
            return true;
        }
        LOGGER.info("Scheduler congestion: There are {} prior instances running for setup objectRef {}", setupRef)
        // there are prior instances, and we expect some action required
        val srq                 = new ServiceRequest
        srq.requestHeader       = header
        srq.requestParameters   = new DealWithPriorJobInstancesRequest(setupRef)
        srq.authentication      = auth
        val resp = inProcessExecutor.executeTrusted(srq)
        if (resp instanceof DealWithPriorJobInstancesResponse) {
            return resp.invokeNewInstance
        } else {
            LOGGER.error("Bad response from DealWithPriorJobInstancesRequest: {}", resp)
            return true
        }
    }
}
