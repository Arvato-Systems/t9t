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
package com.arvatosystems.t9t.email.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.email.EmailAttachmentsKey
import com.arvatosystems.t9t.email.EmailRef
import com.arvatosystems.t9t.email.jpa.entities.EmailAttachmentsEntity
import com.arvatosystems.t9t.email.jpa.entities.EmailEntity
import com.arvatosystems.t9t.email.jpa.entities.EmailModuleCfgEntity

@AutoResolver42
class EmailResolvers {
    @AllCanAccessGlobalTenant
    def EmailModuleCfgEntity      getEmailModuleCfgEntity  (Long key,                boolean onlyActive) {}

    def EmailEntity               getEmailEntity           (EmailRef ref,            boolean onlyActive) {}
    def EmailAttachmentsEntity    getEmailAttachmentsEntity(EmailAttachmentsKey key, boolean onlyActive) {}
}
