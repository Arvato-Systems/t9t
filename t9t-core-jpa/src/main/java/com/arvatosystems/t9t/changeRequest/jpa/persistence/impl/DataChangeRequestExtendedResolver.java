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
