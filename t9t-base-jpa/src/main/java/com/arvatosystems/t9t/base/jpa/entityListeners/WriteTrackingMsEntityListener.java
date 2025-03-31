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
package com.arvatosystems.t9t.base.jpa.entityListeners;

import com.arvatosystems.t9t.base.entities.FullTrackingMs;
import com.arvatosystems.t9t.base.entities.WriteTrackingMs;

import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import jakarta.persistence.PrePersist;

public class WriteTrackingMsEntityListener extends AbstractEntityListener<WriteTrackingMs> {

    @PrePersist
    @Override
    public void prePersist(final BonaPersistableTracking<WriteTrackingMs> entity) {
        final FullTrackingMs tr = new FullTrackingMs();
        createTracking(tr);
        entity.put$Tracking(tr);
    }

    @Override
    public void preUpdate(final BonaPersistableTracking<WriteTrackingMs> entity) {
    }

}
