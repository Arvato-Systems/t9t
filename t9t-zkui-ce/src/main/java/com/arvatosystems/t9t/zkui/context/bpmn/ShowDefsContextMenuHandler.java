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
package com.arvatosystems.t9t.zkui.context.bpmn;

import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.util.JumpTool;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("bpmnStatus.ctx.toBpmnDef")
public class ShowDefsContextMenuHandler implements IGridContextMenu<ProcessExecutionStatusDTO> {

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<ProcessExecutionStatusDTO, TrackingBase> dwt) {
        final ProcessExecutionStatusDTO dto = dwt.getData();
        JumpTool.jump("screens/data_admin/processDefinition28.zul", "processDefinitionId", dto.getProcessDefinitionId(), "screens/data_admin/bpmnStatus28.zul");
    }
}
