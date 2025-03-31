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

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import com.arvatosystems.t9t.io.request.UpdateAsyncMessageStatusRequest;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

// the "mark as unsent actively performs a resend / requeue
@Singleton
@Named("asyncMessage.ctx.markAsUnsent")
public class AsyncMessageMarkAsUnsentContextHandler implements IGridContextMenu<AsyncMessageDTO> {
    protected final IT9tRemoteUtils remoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);

    @Override
    public boolean isEnabled(final DataWithTracking<AsyncMessageDTO, TrackingBase> dwt) {
        return true; // dwt.getData().getStatus() == ExportStatusEnum.RESPONSE_OK; // for stuck messages, resend from any state should be possible
    }

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<AsyncMessageDTO, TrackingBase> dwt) {
        final AsyncMessageDTO dto = dwt.getData();
        final UpdateAsyncMessageStatusRequest rq = new UpdateAsyncMessageStatusRequest();
        rq.setAsyncMessageRef(dto.getObjectRef());
        rq.setNewStatus(ExportStatusEnum.READY_TO_EXPORT);
        remoteUtils.executeExpectOk(rq);
    }
}
