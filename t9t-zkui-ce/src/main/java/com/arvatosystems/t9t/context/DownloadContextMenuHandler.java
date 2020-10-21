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

import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.services.IT9TMessagingDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("sinkSearch.ctx.download")
public class DownloadContextMenuHandler implements IGridContextMenu<SinkDTO> {
    protected final IT9TMessagingDAO messagingDAO = Jdp.getRequired(IT9TMessagingDAO.class);

    @Override
    public void selected(Grid28 lb, DataWithTracking<SinkDTO, TrackingBase> dwt) {
        SinkDTO dto = dwt.getData();
        try {
            messagingDAO.downloadFileAndSave(dto.getObjectRef());
        } catch (ReturnCodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
