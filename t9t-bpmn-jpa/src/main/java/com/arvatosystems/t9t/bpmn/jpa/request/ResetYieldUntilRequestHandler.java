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
package com.arvatosystems.t9t.bpmn.jpa.request;

import java.time.Instant;
import java.util.List;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessExecStatusEntity;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessExecStatusEntityResolver;
import com.arvatosystems.t9t.bpmn.request.ResetYieldUntilRequest;

import de.jpaw.dp.Jdp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

public class ResetYieldUntilRequestHandler extends AbstractRequestHandler<ResetYieldUntilRequest> {

    private final IProcessExecStatusEntityResolver processExecStatusResolver = Jdp.getRequired(IProcessExecStatusEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ResetYieldUntilRequest request) throws Exception {
        final List<ProcessExecStatusEntity> current = processExecStatusResolver.findByTargetObjectRefAndProcessDefinitionId(false,
            request.getTargetObjectRef(), request.getProcessDefinitionId());
        final ServiceResponse resp = new ServiceResponse();
        if (current.isEmpty()) {
            resp.setReturnCode(1);
        } else {
            final ProcessExecStatusEntity ps = current.get(0);
            final EntityManager em = processExecStatusResolver.getEntityManager();
            ctx.lockRef(ps.getObjectRef());  // prevent that it's currently being executed
            try {
                em.refresh(ps);  // avoid any optimistic locking exception
                ps.setYieldUntil(request.getNewYieldUntil() != null ? request.getNewYieldUntil() : Instant.now().plusSeconds(60));
            } catch (final EntityNotFoundException e) {
                // treat it as if it did not exist before
                resp.setReturnCode(1);
            }
        }
        return resp;
    }
}
