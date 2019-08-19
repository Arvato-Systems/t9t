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
package com.arvatosystems.t9t.uiprefsv3.jpa.impl

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.uiprefsv3.jpa.mapping.ILeanGridConfigDTOMapper
import com.arvatosystems.t9t.uiprefsv3.jpa.persistence.ILeanGridConfigEntityResolver
import com.arvatosystems.t9t.uiprefsv3.services.ILeanGridConfigRead
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton

@Singleton
class LeanGridConfigReader implements ILeanGridConfigRead {
    @Inject ILeanGridConfigEntityResolver gridConfigResolver
    @Inject ILeanGridConfigDTOMapper gridConfigMapper

    override readLeanGridConfig(String gridId, Integer variant, Long userRef) {
        val entityClass = gridConfigResolver.entityClass
        val queryString = '''
            SELECT r FROM «entityClass.simpleName» r
             WHERE r.tenantRef    in (:tenantRef,:globalTenantRef)
               AND r.variant      in (:variant,0)
               AND r.userRef      in (:userRef, 0)
               AND r.gridId       in (:gridId)
               ORDER BY r.tenantRef DESC, r.userRef DESC, r.variant DESC
        '''
        val query = gridConfigResolver.entityManager.createQuery(queryString, entityClass)
        query.setParameter("tenantRef",         gridConfigResolver.sharedTenantRef)
        query.setParameter("globalTenantRef",   T9tConstants.GLOBAL_TENANT_REF42)
        query.setParameter("variant",           variant)
        query.setParameter("userRef",           userRef)
        query.setParameter("gridId",           gridId)

        val result = query.resultList
        if (result.isEmpty)
            return null
        else
            return gridConfigMapper.mapToDto(result.get(0))
    }
}
