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

import java.io.StringWriter;

import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.components.basic.ModalWindows;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.xml.bind.JAXB;

@Singleton
@Named("asyncMessage.ctx.showAsyncRqAsXml")
public class AsyncMessageShowAsXmlContextHandler implements IGridContextMenu<AsyncMessageDTO> {

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<AsyncMessageDTO, TrackingBase> dwt) {
        final AsyncMessageDTO dto = dwt.getData();
        final BonaPortable rp = dto.getPayload();
        if (rp != null) {
            final Info info = new Info();
            final StringWriter sw = new StringWriter();
            JAXB.marshal(rp, sw);
            info.setText(sw.toString());
            ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> { });
        }
    }
}
