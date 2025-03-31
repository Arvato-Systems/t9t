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
package com.arvatosystems.t9t.doc.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoHandler
import com.arvatosystems.t9t.annotations.jpa.active.AutoMap42
import com.arvatosystems.t9t.doc.MailingGroupDTO
import com.arvatosystems.t9t.doc.MailingGroupKey
import com.arvatosystems.t9t.doc.jpa.entities.MailingGroupEntity
import com.arvatosystems.t9t.doc.jpa.persistence.IMailingGroupEntityResolver

@AutoMap42
class MailingGroupEntityMappers {
    IMailingGroupEntityResolver mailingGroupResolver

    @AutoHandler("CRSP42")
    def void e2dMailingGroupDTO(MailingGroupEntity entity, MailingGroupDTO dto) {}

    def void d2eMailingGroupDTO(MailingGroupEntity entity, MailingGroupDTO dto) {}

    def void e2dMailingGroupKey(MailingGroupEntity entity, MailingGroupKey dto) {}
}
