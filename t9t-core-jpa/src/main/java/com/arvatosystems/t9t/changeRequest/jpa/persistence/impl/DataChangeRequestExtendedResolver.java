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
package com.arvatosystems.t9t.changeRequest.jpa.persistence.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestInternalKey;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestKey;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestRef;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import jakarta.annotation.Nonnull;

@Specializes
@Singleton
public class DataChangeRequestExtendedResolver extends DataChangeRequestEntityResolver {

    protected DataChangeRequestRef resolveNestedRefs(@Nonnull final DataChangeRequestRef dcrRef) {
        if (dcrRef instanceof DataChangeRequestKey dcrKey) {
            final DataChangeRequestInternalKey internalKey = new DataChangeRequestInternalKey();
            internalKey.setPqon(dcrKey.getPqon());
            internalKey.setChangeId(dcrKey.getChangeId());
            final BonaPortable key = dcrKey.getKey() == null ? T9tConstants.NO_KEY : dcrKey.getKey();
            internalKey.setKey(CompactByteArrayComposer.marshal(DataChangeRequestDTO.meta$$key, key, false));
            return internalKey;
        }
        return super.resolveNestedRefs(dcrRef);
    }
}
