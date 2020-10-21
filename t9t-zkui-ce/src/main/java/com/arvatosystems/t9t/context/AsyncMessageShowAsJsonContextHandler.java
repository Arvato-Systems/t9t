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

import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.components.ModalWindows;
import com.arvatosystems.t9t.io.AsyncMessageDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposerPrettyPrint;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("asyncMessage.ctx.showAsyncRqAsJson")
public class AsyncMessageShowAsJsonContextHandler implements IGridContextMenu<AsyncMessageDTO> {

    @Override
    public void selected(Grid28 lb, DataWithTracking<AsyncMessageDTO, TrackingBase> dwt) {
        AsyncMessageDTO dto = dwt.getData();
        BonaPortable rp = dto.getPayload();
        if (rp != null) {
            Info info = new Info();
            info.setText(JsonComposerPrettyPrint.toJsonString(rp));
            ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {});
        }
    }
}
