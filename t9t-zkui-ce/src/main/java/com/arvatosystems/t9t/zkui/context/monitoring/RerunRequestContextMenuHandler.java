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
package com.arvatosystems.t9t.zkui.context.monitoring;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.request.RerunRequest;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
@Named("requests.ctx.rerun")
public class RerunRequestContextMenuHandler implements IGridContextMenu<MessageDTO> {
    protected final IT9tRemoteUtils remoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);

    @Override
    public boolean isEnabled(final DataWithTracking<MessageDTO, TrackingBase> dwt) {
        final MessageDTO dto = dwt.getData();
        if (ApplicationException.isOk(dto.getReturnCode()))
            return false;
        if (dto.getRerunByProcessRef() != null)
            return false;
        return true;
    }

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<MessageDTO, TrackingBase> dwt) {
        final MessageDTO dto = dwt.getData();
        final RerunRequest rq = new RerunRequest();
        rq.setProcessRef(dto.getObjectRef());
        remoteUtils.executeExpectOk(rq);
    }
}
