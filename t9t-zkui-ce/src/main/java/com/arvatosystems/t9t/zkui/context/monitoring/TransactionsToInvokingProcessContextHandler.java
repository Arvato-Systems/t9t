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
package com.arvatosystems.t9t.zkui.context.monitoring;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.util.JumpTool;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("requests.ctx.toInvoking")
public class TransactionsToInvokingProcessContextHandler implements IGridContextMenu<MessageDTO> {

    @Override
    public boolean isEnabled(final DataWithTracking<MessageDTO, TrackingBase> dwt) {
        final MessageDTO dto = dwt.getData();
        return dto.getInvokingProcessRef() != null;
    }

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<MessageDTO, TrackingBase> dwt) {
        final MessageDTO dto = dwt.getData();
        final int myRtti = dto.ret$rtti();
        final Long invokingProcessRef = dto.getInvokingProcessRef();
        if (invokingProcessRef != null) {
            // parent could be another process or a scheduler setup
            final int targetRtti = (int)(invokingProcessRef % 10000L);
            if (targetRtti == myRtti) {
                // another request
                JumpTool.jump("screens/monitoring/requests28.zul", "objectRef", invokingProcessRef, "screens/monitoring/requests28.zul");
            } else if (targetRtti == SchedulerSetupDTO.class$rtti()) {
                // invoked by the scheduler
                JumpTool.jump("screens/scheduler/schedulerSetup28.zul", "objectRef", invokingProcessRef, "screens/monitoring/requests28.zul");
            }
        }
    }
}
