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
package com.arvatosystems.t9t.auth.jpa.mapping.impl;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserEntity2UserDataMapper;
import com.arvatosystems.t9t.authc.api.UserData;

import de.jpaw.dp.Singleton;

@Singleton
public class UserEntity2UserDataMapper implements IUserEntity2UserDataMapper {

    @Override
    public List<UserData> mapToUserData(final List<UserEntity> users) {
        final List<UserData> userDataList = new ArrayList<UserData>(users.size());

        for (final UserEntity user : users) {
            userDataList.add(mapToUserData(user));
        }

        return userDataList;
    }

    @Override
    public UserData mapToUserData(final UserEntity userEntity) {
        final UserData data = new UserData();

        data.setUserId(userEntity.getUserId());
        data.setIsActive(userEntity.getIsActive());
        data.setZ(userEntity.getZ());
        data.setName(userEntity.getName());
        data.setEmailAddress(userEntity.getEmailAddress());
        data.setOffice(userEntity.getOffice());
        data.setDepartment(userEntity.getDepartment());
        data.setJobTitle(userEntity.getJobTitle());
        data.setPhoneNo(userEntity.getPhoneNo());
        data.setMobilePhoneNo(userEntity.getMobilePhoneNo());
        data.setSalutation(userEntity.getSalutation());
        data.setOrgUnit(userEntity.getOrgUnit());
        data.setUserIdExt(userEntity.getUserIdExt());
        data.setIsTechnical(userEntity.getIsTechnical());
        data.setUserRef(userEntity.getObjectRef());

        return data;
    }

}
