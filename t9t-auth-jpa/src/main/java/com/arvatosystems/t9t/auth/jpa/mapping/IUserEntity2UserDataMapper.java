package com.arvatosystems.t9t.auth.jpa.mapping;

import java.util.List;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.authc.api.UserData;

public interface IUserEntity2UserDataMapper {

    List<UserData> mapToUserData(List<UserEntity> users);

    UserData mapToUserData(UserEntity userEntity);
}
