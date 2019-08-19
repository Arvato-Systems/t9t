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

import com.arvatosystems.t9t.base.request.ProcessStatusDTO
import com.arvatosystems.t9t.base.request.ProcessStatusRequest
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.JdpThreadLocalStrict
import de.jpaw.dp.Singleton
import java.util.ArrayList
import java.util.List
import org.joda.time.Instant
import com.arvatosystems.t9t.base.request.TerminateProcessRequest
import com.arvatosystems.t9t.base.T9tException

/**
 * An extension of the ThreadLocal scope
 */
@Singleton
@AddLogger
class RequestContextScope extends JdpThreadLocalStrict<RequestContext> {

    override void set(RequestContext ctx) {
        // debug!
        if (instances.get(threadId()) !== null) {
            val String msg = "RequestContext already set - coding problem - probably you called the wrong Exector.executeSynchronous() method"
            LOGGER.error(msg)
            throw new RuntimeException(msg)
        }
        super.set(ctx) // LOGGER.info("XXXXXXXXXXXXXXXX SET");
    }

    def List<ProcessStatusDTO> getProcessStatus(ProcessStatusRequest filters, Instant myExecutionStart) {
        val refAge = myExecutionStart.millis
        val result = new ArrayList<ProcessStatusDTO>(30)
        instances.forEach[ threadId, it |
            val thisAge = refAge - executionStart.millis
            if (thisAge >= filters.minAgeInMs &&
               (filters.tenantId === null || filters.tenantId == tenantId) &&
               (filters.userId   === null || filters.userId == userId)) {
                val hdr                 = internalHeaderParameters
                val dto                 = new ProcessStatusDTO
                dto.threadId            = threadId
                dto.ageInMs             = thisAge
                dto.tenantId            = tenantId
                dto.userId              = userId
                dto.sessionRef          = hdr.jwtInfo.sessionRef
                dto.processRef          = requestRef
                dto.processStartedAt    = executionStart
                dto.pqon                = hdr.requestParameterPqon
                dto.invokingProcessRef  = hdr.requestHeader?.invokingProcessRef
                dto.progressCounter     = progressCounter
                dto.callStack           = getCallStack();
                if (statusText !== null) {
                    dto.statusText      = if (statusText.length <= 512) statusText else statusText.substring(0, 512)
                }
                val threadName = createdByThread.name
                if (threadName !== null) {
                    dto.createdByThread = if (threadName.length <= 64) threadName else threadName.substring(0, 64)
                }
                result.add(dto)
            }
        ]
        return result
    }

    def int terminateRequest(TerminateProcessRequest rq) {
        val processToTerminate = instances.get(rq.threadId)
        if (processToTerminate === null || processToTerminate.tenantId != rq.tenantId || processToTerminate.requestRef != rq.processRef) {
            return T9tException.RECORD_DOES_NOT_EXIST
        }
        processToTerminate.createdByThread.interrupt();
        return 0;
    }
}
