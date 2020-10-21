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
package com.arvatosystems.t9t.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;
import org.zkoss.zul.Window.Mode;

import com.arvatosystems.t9t.components.crud.ModuleConfigVM;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;

@Init(superclass = true)
public class SendTestEmail extends ModuleConfigVM<EmailModuleCfgDTO> {

    @Command
    public void popup() throws ReturnCodeException {
        Window modal = (Window) Executions.createComponents("/context/testEmail.zul", null, null);
        modal.setSizable(true);
        modal.setClosable(true);
        modal.setMode(Mode.MODAL);
        modal.setClientAttribute("noautofocus", "true");
    }


}
