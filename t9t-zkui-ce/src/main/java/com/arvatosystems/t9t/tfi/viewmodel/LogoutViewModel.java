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
package com.arvatosystems.t9t.tfi.viewmodel;

import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;

import com.arvatosystems.t9t.tfi.general.Constants;

/**
 * Logout ViewModel.
 *
 * @author INCI02
 *
 */
public class LogoutViewModel extends GenericForwardComposer<Component> {
    private static final long serialVersionUID = -3398694299050788517L;

    public LogoutViewModel() {}

    /**
     *
     * @param comp clicked label / link
     * @throws Exception .
     */
    @Override
    public final void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    /**
     * On link clicked Shiro logoff.
     */
    @GlobalCommand("logoff")
    public final void onClick$logoff() {
        Executions.sendRedirect(Constants.ZulFiles.LOGOUT);
    }
}
