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
package com.arvatosystems.t9t.batch.jpa.impl;

import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;
import com.arvatosystems.t9t.batch.RecordEventsDTO;
import com.arvatosystems.t9t.batch.RecordEventsRef;
import com.arvatosystems.t9t.batch.jpa.entities.RecordEventsEntity;
import com.arvatosystems.t9t.batch.jpa.mapping.IRecordEventsDTOMapper;
import com.arvatosystems.t9t.batch.jpa.persistence.IRecordEventsEntityResolver;
import com.arvatosystems.t9t.batch.services.IRecordEventsResolver;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class RecordEventsResolver extends AbstractJpaResolver<RecordEventsRef, RecordEventsDTO, WriteTracking, RecordEventsEntity>
    implements IRecordEventsResolver {

    public RecordEventsResolver() {
        super("RecordEvents", Jdp.getRequired(IRecordEventsEntityResolver.class), Jdp.getRequired(IRecordEventsDTOMapper.class));
    }

    @Override
    public RecordEventsRef createKey(final Long ref) {
        return ref == null ? null : new RecordEventsRef(ref);
    }
}
