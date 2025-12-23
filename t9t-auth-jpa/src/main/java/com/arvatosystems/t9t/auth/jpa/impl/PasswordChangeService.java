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
package com.arvatosystems.t9t.auth.jpa.impl;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.PasswordUtil;
import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.auth.jpa.IPasswordChangeService;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IPasswordEntityResolver;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IAuthSessionService;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import java.time.Instant;
import java.util.List;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PasswordChangeService extends AbstractPasswordService implements IPasswordChangeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChangeService.class);

    protected final IPasswordEntityResolver passwordResolver = Jdp.getRequired(IPasswordEntityResolver.class);
    protected final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);

    @IsLogicallyFinal
    protected IAuthSessionService authSessionService;

    @Override
    public void changePassword(String newPassword, UserEntity userEntity, UserStatusEntity userStatusEntity) {
        AuthModuleCfgDTO authModuleCfg = authModuleCfgResolver.getModuleConfiguration() == null ? IAuthPersistenceAccess.DEFAULT_MODULE_CFG
                : authModuleCfgResolver.getModuleConfiguration();
        // minimum check length
        validatePasswordLength(authModuleCfg.getPasswordMinimumLength(), newPassword);

        // password must be checked against blacklist
        checkPasswordAgainstBlacklist(newPassword);

        // new password must differ from n previous password
        final ByteArray newPasswordHash = PasswordUtil.createPasswordHash(userEntity.getUserId(), newPassword);
        validatePasswordDifference(authModuleCfg.getPasswordDifferPreviousN(), newPasswordHash, userEntity);

        // check if we can reuse password for its blocking
        validateReusePasswordBlocking(authModuleCfg.getPasswordBlockingPeriod(), newPasswordHash, userEntity);

        // finally change password
        // make modification to userStatus too
        userStatusEntity.setCurrentPasswordSerialNumber(userStatusEntity.getCurrentPasswordSerialNumber() + 1);

        final PasswordEntity newPasswordEntity = passwordResolver.newEntityInstance();
        newPasswordEntity.setObjectRef(userEntity.getObjectRef());
        newPasswordEntity.setPasswordSetByUser(userEntity.getObjectRef());
        newPasswordEntity.setPasswordHash(newPasswordHash);
        newPasswordEntity.setPasswordCreation(Instant.now());
        newPasswordEntity.setPasswordExpiry(
                newPasswordEntity.getPasswordCreation().plusSeconds(T9tConstants.ONE_DAY_IN_S * authModuleCfg.getPasswordExpirationInDays()));
        newPasswordEntity.setUserExpiry(newPasswordEntity.getPasswordCreation()
                .plusSeconds(T9tConstants.ONE_DAY_IN_S * T9tConstants.DEFAULT_MAXIUM_NUMBER_OF_DAYS_IN_BETWEEN_USER_ACTIVITIES));
        newPasswordEntity.setPasswordSerialNumber(userStatusEntity.getCurrentPasswordSerialNumber());
        passwordResolver.save(newPasswordEntity);
        getAuthSessionService().userSessionInvalidationOnCurrentServer(contextProvider.get(), userEntity.getUserId(), false);

        LOGGER.debug("User {} password has successfully been changed", userEntity.getUserId());
    }

    private void validatePasswordLength(Integer passwordMinimumLength, String newPassword) {
        if (passwordMinimumLength != null && passwordMinimumLength > 0) {
            if (newPassword.length() < passwordMinimumLength) {
                LOGGER.error("Password doesn't fulfill the minimum length of {}", passwordMinimumLength);
                throw new T9tException(T9tAuthException.PASSWORD_VALIDATION_FAILED);
            }
        }
    }

    private void validatePasswordDifference(Integer passwordDifferPreviousN, ByteArray newPasswordHash, UserEntity userEntity) {
        if (passwordDifferPreviousN != null && passwordDifferPreviousN > 0) {
            final TypedQuery<PasswordEntity> query = passwordResolver.getEntityManager().createQuery(
                "SELECT p FROM PasswordEntity p WHERE p.objectRef = ?1 ORDER BY p.passwordSerialNumber DESC", PasswordEntity.class);
            query.setParameter(1, userEntity.getObjectRef());
            query.setMaxResults(passwordDifferPreviousN);
            final List<PasswordEntity> results = query.getResultList();
            for (PasswordEntity oldPassword : results) {
                if (oldPassword.getPasswordHash().equals(newPasswordHash)) {
                    LOGGER.error("Password should differ from its previous entry for {} times", passwordDifferPreviousN);
                    throw new T9tException(T9tAuthException.PASSWORD_VALIDATION_FAILED);
                }
            }
        }
    }

    private void validateReusePasswordBlocking(Integer passwordBlockingPeriod, ByteArray newPassswordHash, UserEntity userEntity) {
        if (passwordBlockingPeriod != null && passwordBlockingPeriod > 0) {
            final TypedQuery<PasswordEntity> query = passwordResolver.getEntityManager().createQuery(
                "SELECT p FROM PasswordEntity p WHERE p.objectRef = ?1 AND p.passwordHash = :passwordHash", PasswordEntity.class);
            query.setParameter(1, userEntity.getObjectRef());
            query.setParameter("passwordHash", newPassswordHash);

            final PasswordEntity matchedPassword = query.getSingleResult();
            final Instant earliestDatePasswordCanBeUsedAgain = matchedPassword.getPasswordCreation()
                    .plusSeconds(T9tConstants.ONE_DAY_IN_S * passwordBlockingPeriod);
            if (earliestDatePasswordCanBeUsedAgain.isAfter(Instant.now())) {
                LOGGER.error("Can't reuse password before {}", earliestDatePasswordCanBeUsedAgain);
                throw new T9tException(T9tAuthException.PASSWORD_VALIDATION_FAILED);
            }
        }
    }

    private IAuthSessionService getAuthSessionService() {
        if (authSessionService == null) {
            authSessionService = Jdp.getRequired(IAuthSessionService.class);
        }
        return authSessionService;
    }
}
