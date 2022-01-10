/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.uiprefsv3.jpa.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO;
import com.arvatosystems.t9t.uiprefsv3.jpa.entities.LeanGridConfigEntity;
import com.arvatosystems.t9t.uiprefsv3.jpa.mapping.ILeanGridConfigDTOMapper;
import com.arvatosystems.t9t.uiprefsv3.jpa.persistence.ILeanGridConfigEntityResolver;
import com.arvatosystems.t9t.uiprefsv3.services.ILeanGridConfigRead;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.util.List;

import javax.persistence.TypedQuery;

@Singleton
public class LeanGridConfigReader implements ILeanGridConfigRead {
    private final ILeanGridConfigEntityResolver gridConfigResolver = Jdp.getRequired(ILeanGridConfigEntityResolver.class);
    private final ILeanGridConfigDTOMapper gridConfigMapper = Jdp.getRequired(ILeanGridConfigDTOMapper.class);

    @Override
    public LeanGridConfigDTO readLeanGridConfig(final String gridId, final Integer variant, final Long userRef) {
        final Class<LeanGridConfigEntity> entityClass = gridConfigResolver.getEntityClass();
        final String queryString = "SELECT r FROM " + entityClass.getSimpleName() + " r"
                                + " WHERE r.tenantRef    in (:tenantRef,:globalTenantRef)"
                                + "   AND r.variant      in (:variant,0)"
                                + "   AND r.userRef      in (:userRef, 0)"
                                + "   AND r.gridId       in (:gridId)"
                                + "   ORDER BY r.tenantRef DESC, r.userRef DESC, r.variant DESC";
        final TypedQuery<LeanGridConfigEntity> query = gridConfigResolver.getEntityManager().createQuery(queryString, entityClass);
        query.setParameter("tenantRef", gridConfigResolver.getSharedTenantRef());
        query.setParameter("globalTenantRef", T9tConstants.GLOBAL_TENANT_REF42);
        query.setParameter("variant", variant);
        query.setParameter("userRef", userRef);
        query.setParameter("gridId", gridId);

        final List<LeanGridConfigEntity> result = query.getResultList();
        if (result.isEmpty()) {
            return null;
        }
        return gridConfigMapper.mapToDto(result.get(0));
    }
}
