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
package com.arvatosystems.t9t.doc.jpa.request;

import com.arvatosystems.t9t.base.jpa.impl.AbstractLeanSearchRequestHandler;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.doc.jpa.entities.MailingGroupEntity;
import com.arvatosystems.t9t.doc.jpa.persistence.IMailingGroupEntityResolver;
import com.arvatosystems.t9t.doc.request.LeanMailingGroupSearchRequest;

import de.jpaw.dp.Jdp;

public class LeanMailingGroupSearchRequestHandler extends AbstractLeanSearchRequestHandler<LeanMailingGroupSearchRequest, MailingGroupEntity> {
    public LeanMailingGroupSearchRequestHandler() {
        super(Jdp.getRequired(IMailingGroupEntityResolver.class), (final MailingGroupEntity mailingGroupEntity) -> {
            return new Description(mailingGroupEntity.getObjectRef(), mailingGroupEntity.getMailingGroupId(),
                    mailingGroupEntity.getDescription() == null ? mailingGroupEntity.getMailingGroupId() : mailingGroupEntity.getDescription(), false, false);
        });
    }
}
