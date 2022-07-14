/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.authc.be.api;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.services.IUserResolver;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import de.jpaw.dp.Jdp;

public abstract class AbstractGetUserDataRequestHandler<T extends RequestParameters> extends AbstractReadOnlyRequestHandler<T> {
    protected final IUserResolver resolver = Jdp.getRequired(IUserResolver.class);

    protected void mapUserData(final UserData data, final UserDTO dto) {
        data.setUserId(dto.getUserId());
        data.setIsActive(dto.getIsActive());
        data.setZ(dto.getZ());
        data.setName(dto.getName());
        data.setEmailAddress(dto.getEmailAddress());
        data.setOffice(dto.getOffice());
        data.setDepartment(dto.getDepartment());
        data.setJobTitle(dto.getJobTitle());
        data.setPhoneNo(dto.getPhoneNo());
        data.setMobilePhoneNo(dto.getMobilePhoneNo());
        data.setSalutation(dto.getSalutation());
        data.setOrgUnit(dto.getOrgUnit());
        data.setUserIdExt(dto.getUserIdExt());
        data.setIsTechnical(dto.getIsTechnical());
    }

    protected UserData responseFromDto(final UserDTO dto) {
        final UserData data = new UserData();
        this.mapUserData(data, dto);
        data.setUserRef(dto.getObjectRef());
        return data;
    }
}
