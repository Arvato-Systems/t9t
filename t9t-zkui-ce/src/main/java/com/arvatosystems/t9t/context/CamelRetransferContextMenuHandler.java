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

import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.ProcessCamelRouteRequest;
import com.arvatosystems.t9t.services.T9TRemoteUtils;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("sinkSearch.ctx.camelRetransfer")
public class CamelRetransferContextMenuHandler implements IGridContextMenu<SinkDTO> {
    protected final T9TRemoteUtils messaging = Jdp.getRequired(T9TRemoteUtils.class);

    @Override
    public boolean isEnabled(DataWithTracking<SinkDTO, TrackingBase> dwt) {
        ExportStatusEnum status = dwt.getData().getCamelTransferStatus();
        return status != null && status != ExportStatusEnum.RESPONSE_OK;
    }

    @Override
    public void selected(Grid28 lb, DataWithTracking<SinkDTO, TrackingBase> dwt) {
        messaging.executeExpectOk(new ProcessCamelRouteRequest(dwt.getData().getObjectRef()));
    }
}
