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

import java.util.List;
import java.util.Locale;

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordBlacklistEntity;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

public class AbstractPasswordService {

    protected final IAuthModuleCfgDtoResolver authModuleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);
    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    protected void checkPasswordAgainstBlacklist(final String newPassword) {

        final AuthModuleCfgDTO authModuleCfg = authModuleCfgResolver.getModuleConfiguration();

        final String checkedPassword = Boolean.TRUE.equals(authModuleCfg.getPasswordCheckCaseInsensitive())
            ? newPassword.toUpperCase(Locale.getDefault())        // NOTE: now the check expects all passwords in the blacklist database table converted to UPPER case to optimize runtime!
            : newPassword;

        final String sqlCondition = Boolean.TRUE.equals(authModuleCfg.getPasswordCheckStartWith())
            ? "WHERE :checkedPassword >= pb.passwordInBlacklist AND :checkedPassword < concat(pb.passwordInBlacklist, chr(255))"
            : "WHERE :checkedPassword = pb.passwordInBlacklist";

        final EntityManager em = jpaContextProvider.get().getEntityManager();

        final TypedQuery<PasswordBlacklistEntity> query = em.createQuery("SELECT pb FROM PasswordBlacklistEntity pb " + sqlCondition,
                PasswordBlacklistEntity.class);
        query.setParameter("checkedPassword", checkedPassword);
        query.setMaxResults(1);

        final List<PasswordBlacklistEntity> results = query.getResultList();
        if (results.size() > 0) {
            // found matching blacklist password
            throw new T9tException(T9tAuthException.PASSWORD_FOUND_IN_BLACKLIST);
        }
    }
}
