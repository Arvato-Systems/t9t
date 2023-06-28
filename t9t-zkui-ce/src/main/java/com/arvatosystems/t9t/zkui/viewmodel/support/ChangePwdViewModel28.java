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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zul.Messagebox;
import org.zkoss.zk.ui.WrongValueException;

import com.arvatosystems.t9t.auth.request.GetPasswordChangeRequirementsResponse;
import com.arvatosystems.t9t.base.auth.ChangePasswordUI;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.util.PasswordUtils;
import com.arvatosystems.t9t.zkui.viewmodel.AbstractViewOnlyVM;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

public class ChangePwdViewModel28 extends AbstractViewOnlyVM<ChangePasswordUI, TrackingBase> {

    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
    private List<String> passwordRequirements;
    private GetPasswordChangeRequirementsResponse response;
    private static final String CHANGE_PWD_VIEW_MODEL = "changePwd";

    @Init
    void init() throws ReturnCodeException {
        super.setInitial("changePwd");
        response = userDAO.getPasswordChangeRequirements();
        constructRequirementsMessage();
    }

    private void constructRequirementsMessage() {
        passwordRequirements = new ArrayList<>(10);
        if (response != null) {
            if (response.getPasswordDifferPreviousN() > 0) {
                addToRequirements("passwordDifferPreviousN", response.getPasswordDifferPreviousN());
            }
            if (response.getPasswordMinimumLength() > 0) {
                addToRequirements("passwordMinimumLength", response.getPasswordMinimumLength());
            }
            if (response.getPasswordMinDigits() > 0) {
                addToRequirements("passwordMinDigits", response.getPasswordMinDigits());
            }
            if (response.getPasswordMinLetters() > 0) {
                addToRequirements("passwordMinLetters", response.getPasswordMinLetters());
            }
            if (response.getPasswordMinOtherChars() > 0) {
                addToRequirements("passwordMinOtherChars", response.getPasswordMinOtherChars());
            }
            if (response.getPasswordMinUppercase() > 0) {
                addToRequirements("passwordMinUppercase", response.getPasswordMinUppercase());
            }
            if (response.getPasswordMinLowercase() > 0) {
                addToRequirements("passwordMinLowercase", response.getPasswordMinLowercase());
            }
            if (response.getPasswordMaxCommonSubstring() > 0) {
                addToRequirements("passwordMaxCommonSubstring", response.getPasswordMaxCommonSubstring());
            }
        }
    }

    private void addToRequirements(String key, int param) {
        passwordRequirements.add(session.translate(CHANGE_PWD_VIEW_MODEL, key, param));
    }

    @NotifyChange("data")
    @Command
    public void saveData() throws ReturnCodeException {

        if (!data.getNewPassword().equals(data.getRetypePassword())) {
            throw new WrongValueException(session.translate("changePwd", "password.mismatch"));
        }

        final PasswordUtils passwordUtils = new PasswordUtils(response.getPasswordMinimumLength(),
                response.getPasswordMinLowercase(),
                response.getPasswordMinUppercase(),
                response.getPasswordMinDigits(),
                response.getPasswordMinLetters(),
                response.getPasswordMinOtherChars(),
                response.getPasswordMaxCommonSubstring());

        if (!passwordUtils.verifyPasswordStrength(data.getOldPassword(), data.getNewPassword())) {
            showRequirementNotMatchError();
        }
        try {
            userDAO.changePassword(data.getOldPassword(), data.getNewPassword());
            postProcessHook();
            session.setPasswordExpires(null);
        } catch (ReturnCodeException rce) {
            showRequirementNotMatchError();
        }
    }

    private void showRequirementNotMatchError() {
        final String message = session.translate("changePwd", "requirementNotMatch");
        throw new WrongValueException(message);
    }

    public void postProcessHook() {
        Messagebox.show(session.translate("changePwd", "success"));
        reset();
    }

    @Command
    @NotifyChange("data")
    public void reset() {
        clearData();
    }

    public List<String> getPasswordRequirements() {
        return passwordRequirements;
    }
}
