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

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.request.ProcessStatusRequest;
import com.arvatosystems.t9t.base.request.TerminateProcessRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.server.InternalHeaderParameters;

import de.jpaw.dp.JdpThreadLocalStrict;
import de.jpaw.dp.Singleton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of the ThreadLocal scope
 */
@Singleton
public class RequestContextScope extends JdpThreadLocalStrict<RequestContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestContextScope.class);

    @Override
    public void set(final RequestContext ctx) {
        // debug!
        if (instances.get(threadId()) != null) {
            final String msg = "RequestContext already set - coding problem - probably you called the wrong Exector.executeSynchronous() method";
            LOGGER.error(msg);
            throw new RuntimeException(msg);
        }
        super.set(ctx);
    }

    public List<ProcessStatusDTO> getProcessStatus(final ProcessStatusRequest filters, final Instant myExecutionStart) {
        final long refAge = myExecutionStart.toEpochMilli();
        final List<ProcessStatusDTO> result = new ArrayList<ProcessStatusDTO>(30);
        instances.forEach((final Long threadId, final RequestContext requestContext) -> {
            final long thisAge = refAge - requestContext.executionStart.toEpochMilli();
            if (thisAge >= filters.getMinAgeInMs()
              && (filters.getTenantId() == null || filters.getTenantId().equals(requestContext.tenantId))
              && (filters.getUserId()   == null || filters.getUserId().equals(requestContext.userId))) {
                final InternalHeaderParameters hdr = requestContext.internalHeaderParameters;
                final ProcessStatusDTO dto = new ProcessStatusDTO();
                dto.setThreadId(threadId);
                dto.setAgeInMs(thisAge);
                dto.setTenantId(requestContext.tenantId);
                dto.setUserId(requestContext.userId);
                dto.setSessionRef(hdr.getJwtInfo().getSessionRef());
                dto.setProcessRef(requestContext.getRequestRef());
                dto.setProcessStartedAt(requestContext.getExecutionStart());
                dto.setPqon(hdr.getRequestParameterPqon());
                dto.setInvokingProcessRef(hdr.getRequestHeader() == null ? null : hdr.getRequestHeader().getInvokingProcessRef());
                dto.setProgressCounter(requestContext.getProgressCounter());
                dto.setCallStack(requestContext.getCallStack());
                dto.setStatusText(MessagingUtil.truncField(requestContext.statusText, ProcessStatusDTO.meta$$statusText.getLength())); // 512
                final String threadName = requestContext.createdByThread.getName();
                dto.setCreatedByThread(MessagingUtil.truncField(threadName, ProcessStatusDTO.meta$$createdByThread.getLength())); // 64

                result.add(dto);
            }
        });
        return result;
    }

    public List<ProcessStatusDTO> getProcessStatusForScheduler(final Long forInvokingProcessRef) {
        final List<ProcessStatusDTO> result = new ArrayList<ProcessStatusDTO>(8);
        final long refAge = Instant.now().toEpochMilli();
        instances.forEach((final Long threadId, final RequestContext requestContext) -> {
            final InternalHeaderParameters hdr = requestContext.internalHeaderParameters;
            Long invokingProcessRef = hdr.getRequestHeader() == null ? null : hdr.getRequestHeader().getInvokingProcessRef();
            if (forInvokingProcessRef.equals(invokingProcessRef)) {
                final ProcessStatusDTO dto = new ProcessStatusDTO();
                dto.setThreadId(threadId);
                dto.setAgeInMs(refAge - requestContext.getExecutionStart().toEpochMilli());
                dto.setTenantId(requestContext.tenantId);
                dto.setUserId(requestContext.userId);
                dto.setSessionRef(hdr.getJwtInfo().getSessionRef());
                dto.setProcessRef(requestContext.getRequestRef());
                dto.setProcessStartedAt(requestContext.getExecutionStart());
                dto.setPqon(hdr.getRequestParameterPqon());
                dto.setInvokingProcessRef(hdr.getRequestHeader() == null ? null : hdr.getRequestHeader().getInvokingProcessRef());
                dto.setProgressCounter(requestContext.getProgressCounter());
                dto.setCallStack(requestContext.getCallStack());
                dto.setStatusText(MessagingUtil.truncField(requestContext.statusText, ProcessStatusDTO.meta$$statusText.getLength())); // 512
                final String threadName = requestContext.createdByThread.getName();
                dto.setCreatedByThread(MessagingUtil.truncField(threadName, ProcessStatusDTO.meta$$createdByThread.getLength())); // 64

                result.add(dto);
            }
        });
        return result;
    }

    public int numberOfProcesses(final Long onlySessionRef, final String onlyTenantId) {
        int count = 0;
        for (final RequestContext inst : instances.values()) {
            if ((onlySessionRef == null || onlySessionRef.equals(inst.internalHeaderParameters.getJwtInfo().getSessionRef()))
             && (onlyTenantId  == null || onlyTenantId.equals(inst.tenantId))) {
                count += 1;
            }
        }
        return count;
    }

    public int terminateRequest(final TerminateProcessRequest rq) {
        final RequestContext processToTerminate = instances.get(rq.getThreadId());
        if (processToTerminate == null || !processToTerminate.tenantId.equals(rq.getTenantId())
                || processToTerminate.requestRef != rq.getProcessRef().longValue()) {
            return T9tException.RECORD_DOES_NOT_EXIST;
        }
        processToTerminate.createdByThread.interrupt();
        return 0;
    }
}
