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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;

@Init(superclass = true)
public class UserVM extends CrudSurrogateKeyVM<UserRef, UserDTO, FullTrackingWithVersion> {

    protected IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
    protected final ApplicationSession session = ApplicationSession.get();
    private boolean canSetPassword = false;
    private boolean isSessionPermissionExisted = session.getPermissions("user").contains(OperationType.CUSTOM)
            || session.getPermissions("user").contains(OperationType.ADMIN);

    @Override
    protected void clearData() {
        super.clearData();
        if (data.getPermissions() == null) {
            data.setPermissions(new PermissionsDTO());
        }
        canSetPassword = false;
    }

    @Override
    protected void loadData(DataWithTracking<UserDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        if (data.getPermissions() == null) {
            data.setPermissions(new PermissionsDTO());
        }

        boolean isCommonTenantExisted = false;
        for (var tenant : session.getAllowedTenants()) {
            if (StringUtils.equals(tenant.getTenantId(), ((DataWithTrackingS) dwt).getTenantId())) {
                isCommonTenantExisted = true;
                break;
            }
        }

        canSetPassword = isSessionPermissionExisted && isCommonTenantExisted;
    }

    @Command
    public void resetPassword() throws ReturnCodeException {
        if (data.getUserId() != null && data.getEmailAddress() != null)
            userDAO.resetPassword(data.getUserId(), data.getEmailAddress());
    }

    @Command
    public void setPassword() {
        Window popupModal = (Window) Executions.createComponents(
                "/screens/user_admin/setPasswordModal.zul", null, Map.of("user", data));
        if (popupModal != null) {
            popupModal.doModal();
        } else {
            Clients.showNotification("Popup window not found.");
        }
    }

    public boolean isCanSetPassword() {
        return canSetPassword;
    }
}
