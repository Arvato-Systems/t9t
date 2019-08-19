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
package com.arvatosystems.t9t.batch.jpa.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;
import com.arvatosystems.t9t.batch.SliceTrackingDTO;
import com.arvatosystems.t9t.batch.SliceTrackingRef;
import com.arvatosystems.t9t.batch.jpa.entities.SliceTrackingEntity;
import com.arvatosystems.t9t.batch.jpa.mapping.ISliceTrackingDTOMapper;
import com.arvatosystems.t9t.batch.jpa.persistence.ISliceTrackingEntityResolver;
import com.arvatosystems.t9t.batch.services.ISliceTrackingResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class SliceTrackingResolver extends AbstractJpaResolver<SliceTrackingRef, SliceTrackingDTO, FullTrackingWithVersion, SliceTrackingEntity> implements ISliceTrackingResolver {

    public SliceTrackingResolver() {
        super("SliceTracking", Jdp.getRequired(ISliceTrackingEntityResolver.class), Jdp.getRequired(ISliceTrackingDTOMapper.class));
    }

    @Override
    public SliceTrackingRef createKey(Long ref) {
        return ref == null ? null : new SliceTrackingRef(ref);
    }
}
