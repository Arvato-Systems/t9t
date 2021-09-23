/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import java.time.Instant;

import com.arvatosystems.t9t.base.entities.MessageTracking;

import de.jpaw.bonaparte.jpa.BonaPersistableTracking;

public class MessageTrackingEntityListener extends AbstractEntityListener<MessageTracking> {

    @PrePersist
    @Override
    public void prePersist(BonaPersistableTracking<MessageTracking> entity) {
        MessageTracking tr = new MessageTracking();
        tr.setCTimestamp(Instant.now());
        tr.setCTechUserId(getCutUserId());
        entity.put$Tracking(tr);
    }

    @Override
    public void preUpdate(BonaPersistableTracking<MessageTracking> entity) {
        //  no updates ?
    }
}
