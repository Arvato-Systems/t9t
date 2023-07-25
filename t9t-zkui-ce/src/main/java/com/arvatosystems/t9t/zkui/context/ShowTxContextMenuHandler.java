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
package com.arvatosystems.t9t.zkui.context;

import com.arvatosystems.t9t.base.entities.FullTracking;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.services.IT9tMessagingDAO;
import com.arvatosystems.t9t.zkui.util.JumpTool;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("sinkSearch.ctx.showTx")
public class ShowTxContextMenuHandler implements IGridContextMenu<SinkDTO> {
    protected final IT9tMessagingDAO messagingDAO = Jdp.getRequired(IT9tMessagingDAO.class);

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<SinkDTO, TrackingBase> dwt) {
        JumpTool.jump("screens/monitoring/requests28.zul", "objectRef", ((FullTracking)(dwt.getTracking())).getcProcessRef(), "screens/report/sink28.zul");
    }
}
