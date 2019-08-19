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
package com.arvatosystems.t9t.auth.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.annotations.jpa.NeedMapping
import com.arvatosystems.t9t.auth.SessionDTO
import com.arvatosystems.t9t.auth.TenantKey
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.jpa.entities.SessionEntity
import com.arvatosystems.t9t.auth.jpa.persistence.ISessionEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver

@AutoMap42
class SessionMappers {
    ISessionEntityResolver resolver
    ITenantEntityResolver  tenantResolver
    IUserEntityResolver    userResolver

    @AutoHandler("S42")
    @NeedMapping
    def void e2dSessionDTO(SessionEntity it, SessionDTO dto) {
        val tenant    = tenantResolver.find(tenantRef)
        val user      = userResolver.find(userRef)
        dto.userRef   = new UserKey(userRef, user?.userId ?: '?')
        dto.tenantRef = new TenantKey(tenantRef, tenant?.tenantId ?: '?')
    }
}
