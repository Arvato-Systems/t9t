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
package com.arvatosystems.t9t.core.jpa.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.core.jpa.entities.CannedRequestEntity;
import com.arvatosystems.t9t.core.jpa.mapping.ICannedRequestDTOMapper;
import com.arvatosystems.t9t.core.jpa.persistence.ICannedRequestEntityResolver;
import com.arvatosystems.t9t.core.services.ICannedRequestResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class CannedRequestResolver extends AbstractJpaResolver<CannedRequestRef, CannedRequestDTO, FullTrackingWithVersion, CannedRequestEntity> implements ICannedRequestResolver {

    public CannedRequestResolver() {
        super("CannedRequest", Jdp.getRequired(ICannedRequestEntityResolver.class), Jdp.getRequired(ICannedRequestDTOMapper.class));
    }

    @Override
    public CannedRequestRef createKey(Long ref) {
        return ref == null ? null : new CannedRequestRef(ref);
    }
}
