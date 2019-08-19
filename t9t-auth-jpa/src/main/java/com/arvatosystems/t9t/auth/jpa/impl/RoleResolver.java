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
package com.arvatosystems.t9t.auth.jpa.impl;

import com.arvatosystems.t9t.auth.RoleDTO;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver;
import com.arvatosystems.t9t.auth.services.IRoleResolver;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class RoleResolver extends AbstractJpaResolver<RoleRef, RoleDTO, FullTrackingWithVersion, RoleEntity> implements IRoleResolver {

    public RoleResolver() {
        super("Role", Jdp.getRequired(IRoleEntityResolver.class), Jdp.getRequired(IRoleDTOMapper.class));
    }

    @Override
    public RoleRef createKey(Long ref) {
        return ref == null ? null : new RoleRef(ref);
    }
}
