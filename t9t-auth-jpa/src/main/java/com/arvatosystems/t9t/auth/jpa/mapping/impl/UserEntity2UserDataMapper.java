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
