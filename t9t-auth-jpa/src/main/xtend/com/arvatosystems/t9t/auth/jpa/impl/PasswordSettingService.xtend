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
package com.arvatosystems.t9t.auth.jpa.impl

import com.arvatosystems.t9t.auth.PasswordUtil
import com.arvatosystems.t9t.auth.jpa.IPasswordSettingService
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity
import com.arvatosystems.t9t.auth.jpa.persistence.IPasswordEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton

@AddLogger
@Singleton
class PasswordSettingService implements IPasswordSettingService {
    @Inject IAuthModuleCfgDtoResolver moduleConfigResolver
    @Inject IUserEntityResolver userEntityResolver
    @Inject IPasswordEntityResolver passwordResolver

    override setPasswordForUser(RequestContext ctx, UserEntity userEntity, String newPassword) {
        var int newPasswordNo = 1
        val authModuleCfg     = moduleConfigResolver.moduleConfiguration
        val passwordExpirationInDays = authModuleCfg?.passwordExpirationInDays ?: 60  // TODO: use centrally defined default config

        val userStatusEntity = userEntityResolver.entityManager.find(UserStatusEntity, userEntity.objectRef)
        if (userStatusEntity === null) {
            // first time password! Create a new record
            val newUserStatusEntity         = new UserStatusEntity => [
                objectRef                   = userEntity.objectRef
                currentPasswordSerialNumber = 1
            ]
            userEntityResolver.entityManager.persist(newUserStatusEntity)
        } else {
            newPasswordNo = userStatusEntity.currentPasswordSerialNumber + 1;
            userStatusEntity.currentPasswordSerialNumber = newPasswordNo
        }

        // finally create and store the new password
        val newPasswordEntity = passwordResolver.newEntityInstance => [
            objectRef         = userEntity.objectRef
            passwordSetByUser = ctx.userRef  // only self-reset has userEntity.objectRef here
            passwordHash      = PasswordUtil.createPasswordHash(userEntity.userId, newPassword)
            passwordCreation  = ctx.executionStart
            passwordExpiry    = ctx.executionStart.plus(T9tConstants.ONE_DAY_IN_MS * passwordExpirationInDays)
            userExpiry        = ctx.executionStart.plus(T9tConstants.ONE_DAY_IN_MS * T9tConstants.DEFAULT_MAXIUM_NUMBER_OF_DAYS_IN_BETWEEN_USER_ACTIVITIES)
        ]
        newPasswordEntity.passwordSerialNumber = newPasswordNo
        passwordResolver.save(newPasswordEntity)
        LOGGER.info("Password for user {} has been successfully reset", userEntity.userId)
    }
}
