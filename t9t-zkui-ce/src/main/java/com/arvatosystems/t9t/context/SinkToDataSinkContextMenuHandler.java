/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.components.tools.JumpTool;
import com.arvatosystems.t9t.io.SinkDTO;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("sinkSearch.ctx.showDataSink")
public class SinkToDataSinkContextMenuHandler implements IGridContextMenu<SinkDTO> {

    @Override
    public void selected(Grid28 lb, DataWithTracking<SinkDTO, TrackingBase> dwt) {
        SinkDTO dto = dwt.getData();
        JumpTool.jump("screens/report/dataSink28.zul", "objectRef",
            dto.getDataSinkRef().getObjectRef(), "screens/report/sink28.zul");
    }

}
