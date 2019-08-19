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

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserRef;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.services.IUserResolver;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class UserResolver extends AbstractJpaResolver<UserRef, UserDTO, FullTrackingWithVersion, UserEntity> implements IUserResolver {

    public UserResolver() {
        super("User", Jdp.getRequired(IUserEntityResolver.class), Jdp.getRequired(IUserDTOMapper.class));
    }

    @Override
    protected TypedQuery<UserEntity> createQuery(EntityManager em) {
        return em.createQuery("SELECT e FROM UserEntity e", UserEntity.class);
    }

    @Override
    public UserRef createKey(Long ref) {
        return ref == null ? null : new UserRef(ref);
    }
}
