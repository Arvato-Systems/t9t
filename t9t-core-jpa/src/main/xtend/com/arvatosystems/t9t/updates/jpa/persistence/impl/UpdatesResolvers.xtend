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
package com.arvatosystems.t9t.updates.jpa.persistence.impl

import java.util.List
import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.updates.UpdateStatusRef
import com.arvatosystems.t9t.updates.UpdateStatusLogRef
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusEntity
import com.arvatosystems.t9t.updates.jpa.entities.UpdateStatusLogEntity

@AutoResolver42
class UpdatesResolvers {
    def UpdateStatusEntity       getUpdateStatusEntity              (UpdateStatusRef ref) {}
    def UpdateStatusLogEntity    getUpdateStatusLogEntity           (UpdateStatusLogRef ref) {}
    def UpdateStatusEntity       findByTicketId                     (boolean onlyActive, String ticketId) {}
    def List<UpdateStatusEntity> findByTicketIdsAndUpdateApplyStatus(boolean onlyActive, List<String> ticketId, String updateApplyStatus) {}
}
