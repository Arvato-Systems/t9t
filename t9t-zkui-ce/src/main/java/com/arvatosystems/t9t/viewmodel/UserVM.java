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

import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.components.crud.CrudSurrogateKeyVM;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Jdp;

@Init(superclass = true)
public class UserVM extends CrudSurrogateKeyVM<UserRef, UserDTO, FullTrackingWithVersion> {

    protected IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    @Override
    protected void clearData() {
        super.clearData();
        if (data.getPermissions() == null)
            data.setPermissions(new PermissionsDTO());
    }

    @Override
    protected void loadData(DataWithTracking<UserDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        if (data.getPermissions() == null)
            data.setPermissions(new PermissionsDTO());
    }

    @Command
    public void resetPassword() throws ReturnCodeException {
        if (data.getUserId() != null && data.getEmailAddress() != null)
            userDAO.resetPassword(data.getUserId(), data.getEmailAddress());
    }
}
