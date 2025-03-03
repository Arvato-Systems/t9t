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
package com.arvatosystems.t9t.zkui.context.changeRequest;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestExtendedDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.components.basic.ModalWindows;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import de.jpaw.bonaparte.core.JsonComposerPrettyPrint;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

/**
 * Shows the raw CRUD request of a data change request from {@link DataChangeRequestDTO}.
 */
@Singleton
@Named("dataChangeRequestExtended.ctx.showRawCrudRequest")
public class DataChangeRequestShowRawCrudContextHandler implements IGridContextMenu<DataChangeRequestExtendedDTO> {

    @Override
    public void selected(@Nonnull final Grid28 lb, @Nonnull final DataWithTracking<DataChangeRequestExtendedDTO, TrackingBase> dwt) {
        final RequestParameters crudRequest = dwt.getData().getChange().getCrudRequest();
        final Info info = new Info();
        info.setText(JsonComposerPrettyPrint.toJsonString(crudRequest));
        ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {
        });
    }
}
