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
package com.arvatosystems.t9t.ssm.be.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.request.TerminateProcessRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.api.MailToUsersRequest;
import com.arvatosystems.t9t.ssm.SchedulerConcurrencyType;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesRequest;
import com.arvatosystems.t9t.ssm.request.DealWithPriorJobInstancesResponse;
import com.arvatosystems.t9t.ssm.services.ISchedulerHook;
import com.arvatosystems.t9t.ssm.services.ISchedulerSetupResolver;

import de.jpaw.dp.Jdp;

public class DealWithPriorJobInstancesRequestHandler extends AbstractRequestHandler<DealWithPriorJobInstancesRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DealWithPriorJobInstancesRequestHandler.class);

    private final ISchedulerSetupResolver schedulerResolver = Jdp.getRequired(ISchedulerSetupResolver.class);
    private final RequestContextScope requestContextScope = Jdp.getRequired(RequestContextScope.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public DealWithPriorJobInstancesResponse execute(final RequestContext ctx, final DealWithPriorJobInstancesRequest rq) throws Exception {
        final SchedulerSetupDTO dto = schedulerResolver.getDTO(rq.getSchedulerSetupRef());
        final List<ProcessStatusDTO> prior = requestContextScope.getProcessStatusForScheduler(dto.getObjectRef());
        final DealWithPriorJobInstancesResponse resp = new DealWithPriorJobInstancesResponse();
        resp.setInvokeNewInstance(true);

        if (prior.isEmpty()) {
            LOGGER.info("No prior instances found for scheduler {}", dto.getSchedulerId());
            return resp;
        }
        final SchedulerConcurrencyType concTypeFresh = dto.getConcurrencyType() == null ? SchedulerConcurrencyType.RUN_PARALLEL : dto.getConcurrencyType();
        final SchedulerConcurrencyType concTypeStale = dto.getConcurrencyTypeStale() == null ? SchedulerConcurrencyType.RUN_PARALLEL : dto.getConcurrencyTypeStale();
        final long ageWhenStale = dto.getTimeLimit() == null ? 0L : dto.getTimeLimit() * 1000L;
        int numStale = 0;
        for (ProcessStatusDTO priorInstance: prior) {
            final SchedulerConcurrencyType concType;
            if (priorInstance.getAgeInMs() < ageWhenStale) {
                concType = concTypeFresh;
            } else {
                ++numStale;
                concType = concTypeStale;
            }
            if (skipRun(ctx, priorInstance, concType, dto)) {
                resp.setInvokeNewInstance(false);
            }
        }
        if (numStale > 0 && dto.getMailingGroupId() != null) {
            LOGGER.info("{} very old instances found - mailing to group {}", numStale, dto.getMailingGroupId());
            final MailToUsersRequest mailRq = new MailToUsersRequest();
            mailRq.setMailingGroupId(dto.getMailingGroupId());
            final Map<String, Object> data = new HashMap<>(4);
            data.put("numStale", numStale);
            data.put("processes", prior);
            mailRq.setData(data);
            executor.executeSynchronous(ctx, mailRq);
        }
        return resp;
    }

    protected boolean skipRun(final RequestContext ctx, ProcessStatusDTO priorInstance, SchedulerConcurrencyType concType, SchedulerSetupDTO dto) {
        switch (concType) {
        case RUN_PARALLEL:
            return false;   // do nothing
        case CUSTOM:
            if (dto.getConcurrencyHook() != null) {
                final ISchedulerHook hook = Jdp.getRequired(ISchedulerHook.class, dto.getConcurrencyHook());
                return !hook.checkPriorInstances(ctx, dto, priorInstance);
            }
            return false;
        case KILL_PREVIOUS:
            LOGGER.info("Killing instance {} of scheduler setup {}", priorInstance.getProcessRef(), dto.getSchedulerId());
            final TerminateProcessRequest killRq = new TerminateProcessRequest();
            killRq.setTenantId(priorInstance.getTenantId());
            killRq.setProcessRef(priorInstance.getProcessRef());
            killRq.setThreadId(priorInstance.getThreadId());
            requestContextScope.terminateRequest(killRq);
            return false;
        case SKIP_INSTANCE:
            return true;
        default:
            break;
        }
        return false;
    }
}
