package com.arvatosystems.t9t.updates.jpa.request;

import com.arvatosystems.t9t.base.jpa.impl.AbstractLeanSearchRequestHandler;
import com.arvatosystems.t9t.base.search.Description;

import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity;
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusEntityResolver;
import com.arvatosystems.t9t.updates.request.LeanUpdateStatusSearchRequest;
import de.jpaw.dp.Jdp;

public class LeanUpdateStatusSearchRequestHandler extends
        AbstractLeanSearchRequestHandler<LeanUpdateStatusSearchRequest, UpdateStatusEntity> {
    public LeanUpdateStatusSearchRequestHandler() {
        super(Jdp.getRequired(IUpdateStatusEntityResolver.class), (final UpdateStatusEntity it) -> {
            return new Description(it.getObjectRef(), it.getTicketId(), it.getDescription(), false, false);
        });
    }
}
