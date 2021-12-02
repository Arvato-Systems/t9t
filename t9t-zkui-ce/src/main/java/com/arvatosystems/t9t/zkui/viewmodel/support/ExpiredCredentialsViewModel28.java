/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.time.Instant;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;

public class ExpiredCredentialsViewModel28 extends ChangePwdViewModel28 {

    private boolean pwdExpired = true;

    @Init
    @Override
    void init() {
        super.init();
        final ApplicationSession as = ApplicationSession.get();
        if (as != null && as.getPasswordExpires() != null && as.getPasswordExpires().isAfter(Instant.now())) {
            pwdExpired = false;
            postProcessHook();
        }
    }

    public boolean isPwdExpired() {
        return pwdExpired;
    }

    public void setPwdExpired(boolean pwdExpired) {
        this.pwdExpired = pwdExpired;
    }

    @Override
    public void postProcessHook() {
        Executions.getCurrent().sendRedirect(Constants.ZulFiles.LOGIN_TENANT_SELECTION);
    };
}
