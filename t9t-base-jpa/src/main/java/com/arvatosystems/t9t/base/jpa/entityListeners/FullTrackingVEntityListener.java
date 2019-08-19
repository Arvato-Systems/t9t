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
package com.arvatosystems.t9t.base.jpa.entityListeners;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;

import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public class FullTrackingVEntityListener extends AbstractEntityListener<FullTrackingWithVersion> {

    @PreUpdate
    @Override
    public void preUpdate(BonaPersistableTracking<FullTrackingWithVersion> entity) {
        FullTrackingWithVersion tr = entity.ret$Tracking();
        updateTracking(tr, true);
        entity.put$Tracking(tr);
    }

    @PrePersist
    @Override
    public void prePersist(BonaPersistableTracking<FullTrackingWithVersion> entity) {
        FullTrackingWithVersion tr = new FullTrackingWithVersion();
        createTracking(tr);
        tr.setVersion(0);
        entity.put$Tracking(tr);
    }
}
