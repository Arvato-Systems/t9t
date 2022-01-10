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
package com.arvatosystems.t9t.auth.jpa.impl;

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.PasswordUtil;
import com.arvatosystems.t9t.auth.jpa.IPasswordSettingService;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IPasswordEntityResolver;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PasswordSettingService implements IPasswordSettingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordSettingService.class);

    protected final IAuthModuleCfgDtoResolver moduleConfigResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);
    protected final IUserEntityResolver userEntityResolver = Jdp.getRequired(IUserEntityResolver.class);
    protected final IPasswordEntityResolver passwordResolver = Jdp.getRequired(IPasswordEntityResolver.class);

    @Override
    public void setPasswordForUser(RequestContext ctx, UserEntity userEntity, String newPassword) {
        int nextPasswordNo = 1;
        final AuthModuleCfgDTO authModuleCfg = moduleConfigResolver.getModuleConfiguration();
        final EntityManager entityManager = userEntityResolver.getEntityManager();

        final UserStatusEntity userStatusEntity = entityManager.find(UserStatusEntity.class, userEntity.getObjectRef());
        if (userStatusEntity == null) {
            // first time password! Create a new record
            final UserStatusEntity newUserStatusEntity = new UserStatusEntity();
            newUserStatusEntity.setObjectRef(userEntity.getObjectRef());
            newUserStatusEntity.setCurrentPasswordSerialNumber(1);
            entityManager.persist(newUserStatusEntity);
        } else {
            nextPasswordNo = userStatusEntity.getCurrentPasswordSerialNumber() + 1;
            userStatusEntity.setCurrentPasswordSerialNumber(nextPasswordNo);
        }

        // finally create and store the new password
        final PasswordEntity newPwdEntity = passwordResolver.newEntityInstance();
        newPwdEntity.setObjectRef(userEntity.getObjectRef());
        newPwdEntity.setPasswordSetByUser(ctx.userRef);
        newPwdEntity.setPasswordHash(PasswordUtil.createPasswordHash(userEntity.getUserId(), newPassword));
        newPwdEntity.setPasswordCreation(ctx.executionStart);
        newPwdEntity.setPasswordExpiry(ctx.executionStart.plusSeconds(T9tConstants.ONE_DAY_IN_S * authModuleCfg.getInitialPasswordExpiration()));
        newPwdEntity.setUserExpiry(
                ctx.executionStart.plusSeconds(T9tConstants.ONE_DAY_IN_S * T9tConstants.DEFAULT_MAXIUM_NUMBER_OF_DAYS_IN_BETWEEN_USER_ACTIVITIES));
        newPwdEntity.setPasswordSerialNumber(nextPasswordNo);
        passwordResolver.save(newPwdEntity);
        LOGGER.info("Password for user {} has been successfully reset", userEntity.getUserId());
    }
}
