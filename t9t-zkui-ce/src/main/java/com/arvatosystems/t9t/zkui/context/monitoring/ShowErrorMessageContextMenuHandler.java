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

import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

@Singleton
@Named("requests.ctx.showMessage")
public class ShowErrorMessageContextMenuHandler implements IGridContextMenu<MessageDTO> {

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<MessageDTO, TrackingBase> dwt) {
        final MessageDTO m = dwt.getData();
        if (m != null && m.getReturnCode() != null) {
            String text = ApplicationException.codeToString(m.getReturnCode());
            String message = "Return code " + m.getReturnCode() + " means " + text;
            Messagebox.show(message, "Information", Messagebox.OK, Messagebox.INFORMATION);
        }
    }
}
