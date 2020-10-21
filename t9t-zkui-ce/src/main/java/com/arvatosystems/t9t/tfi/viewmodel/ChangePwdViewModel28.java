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
import org.zkoss.zk.ui.WrongValueException;

import com.arvatosystems.t9t.base.auth.ChangePasswordUI;
import com.arvatosystems.t9t.components.crud.AbstractViewOnlyVM;
import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

public class ChangePwdViewModel28 extends AbstractViewOnlyVM<ChangePasswordUI, TrackingBase> {

    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    @Init
    void init() {
        super.setInitial("changePwd");
    }

    @NotifyChange("data")
    @Command
    public void saveData() throws ReturnCodeException {

        if (!data.getNewPassword().equals(data.getRetypePassword())) {
            throw new WrongValueException(ApplicationSession.get().translate("changePwd", "password.mismatch"));
        }

        userDAO.changePassword(data.getOldPassword(), data.getNewPassword());
        postProcessHook();
    }

    public void postProcessHook() {
        Messagebox.show(ApplicationSession.get().translate("changePwd", "success"));
        reset();
    }

    @Command
    @NotifyChange("data")
    public void reset() {
        clearData();
    }

}
