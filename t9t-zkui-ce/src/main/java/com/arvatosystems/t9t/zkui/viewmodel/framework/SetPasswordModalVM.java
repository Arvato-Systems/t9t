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

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;


public class SetPasswordModalVM {

    protected IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
    protected final ApplicationSession session = ApplicationSession.get();

    private UserDTO user;
    private String newPassword;
    private String retypePassword;
    private Window windowComponent;

    @Init(superclass = true)
    public void init(@BindingParam("initParams") HashMap<String, Object> initParams, @ContextParam(ContextType.COMPONENT) Component component) {
        windowComponent = (Window) component.getRoot();

        user = (UserDTO) initParams.get("user");
    }

    @Command
    public void saveData() throws ReturnCodeException {
        if (!StringUtils.equals(newPassword, retypePassword)) {
            showError(session.translate("changePwd", "password.mismatch"));
        }

        userDAO.setPassword(user, newPassword);

        Events.sendEvent("onClose", windowComponent, false);
        closeWindow();
    }

    private void showError(final String message) {
        throw new WrongValueException(message);
    }

    @Command
    @NotifyChange({"newPassword", "retypePassword"})
    public void reset() {
        newPassword = null;
        retypePassword = null;
    }

    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, null);
        windowComponent.onClose();
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRetypePassword() {
        return retypePassword;
    }

    public void setRetypePassword(String retypePassword) {
        this.retypePassword = retypePassword;
    }
}
