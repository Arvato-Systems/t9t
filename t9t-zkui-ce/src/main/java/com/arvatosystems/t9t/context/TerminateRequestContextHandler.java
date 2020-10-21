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
package com.arvatosystems.t9t.context;

import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.request.TerminateProcessRequest;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.services.T9TRemoteUtils;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("processStatus.ctx.terminateRequest")
public class TerminateRequestContextHandler implements IGridContextMenu<ProcessStatusDTO> {
    protected final T9TRemoteUtils remoteUtils = Jdp.getRequired(T9TRemoteUtils.class);

    @Override
    public void selected(Grid28 lb, DataWithTracking<ProcessStatusDTO, TrackingBase> dwt) {
        ProcessStatusDTO dto = dwt.getData();
        // create a termination request and send it to the backend
        final TerminateProcessRequest rq = new TerminateProcessRequest();
        rq.setProcessRef(dto.getProcessRef());
        rq.setTenantId(dto.getTenantId());
        rq.setThreadId(dto.getThreadId());
        remoteUtils.executeExpectOk(rq);
    }
}
