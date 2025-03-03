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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigRef;
import com.arvatosystems.t9t.zkui.services.IChangeWorkFlowConfigDAO;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;
import de.jpaw.dp.Jdp;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;

@Init(superclass = true)
public class ChangeWorkFlowConfigVM extends CrudSurrogateKeyVM<ChangeWorkFlowConfigRef, ChangeWorkFlowConfigDTO, FullTrackingWithVersion> {

    protected IChangeWorkFlowConfigDAO changeWorkFlowConfigDAO = Jdp.getRequired(IChangeWorkFlowConfigDAO.class);

    @Command
    @Override
    public void commandSave() {
        super.commandSave();
        changeWorkFlowConfigDAO.invalidateCache();
    }

    @Command
    @Override
    public void commandDelete() {
        super.commandDelete();
        changeWorkFlowConfigDAO.invalidateCache();
    }
}
