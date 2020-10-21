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

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.arvatosystems.t9t.authc.api.ResetPasswordRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.components.crud.AbstractViewOnlyVM;
import com.arvatosystems.t9t.init.ApplicationConfigurationInitializer;
import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

public class ForgotPasswordViewModel28 extends AbstractViewOnlyVM<ResetPasswordRequest, TrackingBase> {

    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
    private final ApplicationConfigurationInitializer initializer = Jdp.getRequired(ApplicationConfigurationInitializer.class);


    @Init
    void init() {
        super.setInitial("resetPwd");
    }

    @NotifyChange("data")
    @Command
    public void saveData() throws ReturnCodeException {
        String forgetPasswordApiKey = initializer.getForgetPasswordApiKey();
        if (forgetPasswordApiKey == null) {
            throw new T9tException(T9tException.NOT_AUTHORIZED, "Configuration missing");
        } else {
              userDAO.getAuthenticationResponse(forgetPasswordApiKey, null);
              userDAO.resetPassword(data.getUserId(), data.getEmailAddress());
              ApplicationSession.get().setJwt(null);
              postProcessHook();
        }
    }

    public void postProcessHook() {
        Messagebox.show(ApplicationSession.get().translate("resetPwd", "success"), ApplicationSession.get().translate("login", "title"), Messagebox.OK, null,
                new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) throws Exception {
                        Executions.getCurrent().sendRedirect(Constants.ZulFiles.LOGIN);
                    }
                });
    }

    @Command
    @NotifyChange("data")
    public void reset() {
        clearData();
    }

}
