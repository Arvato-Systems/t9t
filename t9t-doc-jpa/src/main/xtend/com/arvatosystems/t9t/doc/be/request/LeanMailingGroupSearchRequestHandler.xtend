package com.arvatosystems.t9t.doc.be.request

import com.arvatosystems.t9t.base.jpa.impl.AbstractLeanSearchRequestHandler
import com.arvatosystems.t9t.base.search.Description
import com.arvatosystems.t9t.doc.jpa.entities.MailingGroupEntity
import com.arvatosystems.t9t.doc.jpa.persistence.IMailingGroupEntityResolver
import com.arvatosystems.t9t.doc.request.LeanMailingGroupSearchRequest
import de.jpaw.dp.Jdp

class LeanMailingGroupSearchRequestHandler extends AbstractLeanSearchRequestHandler<LeanMailingGroupSearchRequest, MailingGroupEntity> {
    new() {
        super(Jdp.getRequired(IMailingGroupEntityResolver),
            [ return new Description(objectRef, mailingGroupId, description ?: mailingGroupId, false, false) ]
        )
    }   
}
