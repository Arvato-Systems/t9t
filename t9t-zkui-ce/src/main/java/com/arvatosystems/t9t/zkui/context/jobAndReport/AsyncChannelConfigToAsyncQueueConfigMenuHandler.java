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
package com.arvatosystems.t9t.zkui.context.jobAndReport;

import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.util.JumpTool;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("asyncChannelCfg.ctx.showAsyncQueueCfg")
public class AsyncChannelConfigToAsyncQueueConfigMenuHandler implements IGridContextMenu<AsyncChannelDTO> {

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<AsyncChannelDTO, TrackingBase> dwt) {
        JumpTool.jump("screens/report/asyncQueueCfg28.zul", "objectRef", dwt.getData().getAsyncQueueRef().getObjectRef(),
                "screens/report/asyncChannelCfg28.zul");
    }
}
