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
package com.arvatosystems.t9t.context.bpmn;

import org.joda.time.Instant;

import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.request.ProcessExecutionStatusCrudRequest;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.context.IGridContextMenu;
import com.arvatosystems.t9t.services.T9TRemoteUtils;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("bpmnStatus.ctx.stall")
public class ProcessStallContextMenuHandler implements IGridContextMenu<ProcessExecutionStatusDTO> {
    public static final Instant FAR_AWAY = new Instant((129L * 365L + 24L) * 86400L * 1000L); // 1.1.1970 to 31.12.2099 = 129 years and 24 days
    protected final T9TRemoteUtils remoteUtils = Jdp.getRequired(T9TRemoteUtils.class);

    @Override
    public boolean isEnabled(DataWithTracking<ProcessExecutionStatusDTO, TrackingBase> dwt) {
        Instant current = dwt.getData().getYieldUntil();
        return current.isBefore(FAR_AWAY);
    }

    @Override
    public void selected(Grid28 lb, DataWithTracking<ProcessExecutionStatusDTO, TrackingBase> dwt) {
        ProcessExecutionStatusDTO dto = dwt.getData();
        dto.setYieldUntil(FAR_AWAY);
        ProcessExecutionStatusCrudRequest rq = new ProcessExecutionStatusCrudRequest();
        rq.setCrud(OperationType.UPDATE);
        rq.setData(dto);
        rq.setKey(dto.getObjectRef());
        remoteUtils.executeExpectOk(rq);
    }
}
