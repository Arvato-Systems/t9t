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

import java.time.Instant;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import com.arvatosystems.t9t.base.entities.SessionTracking;

import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public class SessionTrackingEntityListener extends AbstractEntityListener<SessionTracking> {

    @PreUpdate
    @Override
    public void preUpdate(final BonaPersistableTracking<SessionTracking> entity) {
        final SessionTracking tr = entity.ret$Tracking();
        tr.setMTimestamp(Instant.now());
        entity.put$Tracking(tr);
    }

    @PrePersist
    @Override
    public void prePersist(final BonaPersistableTracking<SessionTracking> entity) {
        final SessionTracking tr = entity.ret$Tracking();
        tr.setCTimestamp(Instant.now());
        tr.setMTimestamp(tr.getCTimestamp());
        entity.put$Tracking(tr);
    }
}
