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
import com.arvatosystems.t9t.auth.T9tAuthException
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity
import com.arvatosystems.t9t.auth.jpa.persistence.IPasswordEntityResolver
import com.arvatosystems.t9t.base.T9tConstants
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.util.ByteArray
import org.joda.time.Instant
import org.joda.time.LocalDateTime
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.base.T9tException

@AddLogger
@Singleton
class PasswordChangeService {

    @Inject IPasswordEntityResolver passwordResolver
    @Inject IAuthModuleCfgDtoResolver authModuleCfgResolver

    def changePassword(String newPassword, UserEntity userEntity, UserStatusEntity userStatusEntity) {
        val authModuleCfg = authModuleCfgResolver.moduleConfiguration ?: IAuthPersistenceAccess.DEFAULT_MODULE_CFG

        // minimum check length
        validatePasswordLength(authModuleCfg.passwordMinimumLength, newPassword)

        // new password must differ from n previous password
        val newPasswordHash = PasswordUtil.createPasswordHash(userEntity.userId, newPassword)
        validatePasswordDifference(authModuleCfg.passwordDifferPreviousN, newPasswordHash, userEntity)

        // check if we can reuse password for its blocking
        validateReusePasswordBlocking(authModuleCfg.passwordBlockingPeriod, newPasswordHash, userEntity)

        // finally change password
        // make modification to userStatus too
        userStatusEntity.currentPasswordSerialNumber = userStatusEntity.currentPasswordSerialNumber + 1

        val newPasswordEntity = passwordResolver.newEntityInstance
        newPasswordEntity.objectRef = userEntity.objectRef
        newPasswordEntity.passwordSetByUser = userEntity.objectRef
        newPasswordEntity.passwordHash = newPasswordHash
        newPasswordEntity.passwordCreation = new Instant
        newPasswordEntity.passwordExpiry = new Instant(newPasswordEntity.passwordCreation).plus(T9tConstants.ONE_DAY_IN_MS * authModuleCfg.passwordExpirationInDays)
        newPasswordEntity.userExpiry = new Instant(newPasswordEntity.passwordCreation)
                .plus(T9tConstants.ONE_DAY_IN_MS * T9tConstants.DEFAULT_MAXIUM_NUMBER_OF_DAYS_IN_BETWEEN_USER_ACTIVITIES)
        newPasswordEntity.passwordSerialNumber = userStatusEntity.currentPasswordSerialNumber
        passwordResolver.save(newPasswordEntity)

        LOGGER.debug("User {} password has successfully been changed", userEntity.userId)
    }

    def private validatePasswordLength(Integer passwordMinimumLength, String newPassword) {
        if (passwordMinimumLength !== null && passwordMinimumLength > 0) {
            if (newPassword.length < passwordMinimumLength) {
                LOGGER.error("Password doesn't fulfill the minimum length of {}", passwordMinimumLength)
                throw new T9tException(T9tAuthException.PASSWORD_VALIDATION_FAILED)
            }
        }
    }

    def private validatePasswordDifference(Integer passwordDifferPreviousN, ByteArray newPasswordHash, UserEntity userEntity) {

        if (passwordDifferPreviousN !== null && passwordDifferPreviousN > 0) {

            val query = passwordResolver.entityManager.createQuery(
                "SELECT p FROM PasswordEntity p WHERE p.objectRef = ?1 ORDER BY p.passwordSerialNumber DESC", PasswordEntity)
            query.setParameter(1, userEntity.objectRef)
            query.maxResults = passwordDifferPreviousN

            query.resultList.forEach[oldPassword |
                if (oldPassword.passwordHash.equals(newPasswordHash)) {
                    LOGGER.error("Password should differ from its previous entry for {} times",
                        passwordDifferPreviousN)
                    throw new T9tException(T9tAuthException.PASSWORD_VALIDATION_FAILED)
                }
            ]
        }
    }

    def private validateReusePasswordBlocking(Integer passwordBlockingPeriod, ByteArray newPassswordHash, UserEntity userEntity) {
        if (passwordBlockingPeriod !== null && passwordBlockingPeriod > 0) {
            val query = passwordResolver.entityManager.createQuery(
                "SELECT p FROM PasswordEntity p WHERE p.objectRef = ?1 AND p.passwordHash=:passwordHash", PasswordEntity)
            query.setParameter(1, userEntity.objectRef)
            query.setParameter("passwordHash", newPassswordHash)

            val matchedPassword = query.singleResult
            val earliestDatePasswordCanBeUsedAgain = matchedPassword.passwordCreation.toDateTime.toLocalDateTime
                    .plusDays(passwordBlockingPeriod)
            if (earliestDatePasswordCanBeUsedAgain.isAfter(new LocalDateTime)) {
                LOGGER.error("Can't reuse password before {}", earliestDatePasswordCanBeUsedAgain)
                throw new T9tException(T9tAuthException.PASSWORD_VALIDATION_FAILED)
            }
        }
    }
}
