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
package com.arvatosystems.t9t.rep.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.rep.ReportConfigRef
import com.arvatosystems.t9t.rep.ReportParamsRef
import com.arvatosystems.t9t.rep.jpa.entities.ReportConfigEntity
import com.arvatosystems.t9t.rep.jpa.entities.ReportParamsEntity
import java.util.List
import com.arvatosystems.t9t.rep.jpa.entities.ReportMailingEntity
import com.arvatosystems.t9t.rep.ReportMailingRef

@AutoResolver42
class RepResolvers {
    @AllCanAccessGlobalTenant
    def ReportConfigEntity getReportConfigEntity (ReportConfigRef   entityRef, boolean onlyActive) { return null; }
    def ReportConfigEntity findByReportIdWithDefault(boolean onlyActive, String reportConfigId) { return null; }
    def List<ReportConfigEntity> findByReportIds(boolean onlyActive, List<String> reportConfigId) { return null; }
    def ReportParamsEntity getReportParamsEntity (ReportParamsRef   entityRef, boolean onlyActive) { return null; }
    def ReportParamsEntity findByReportRunId(boolean onlyActive, String reportParamsId) { return null; }
    def ReportMailingEntity getReportMailingEntity (ReportMailingRef   entityRef, boolean onlyActive) { return null; }
    def List<ReportMailingEntity> findByMailingGroupIds(boolean onlyActive, List<String> mailingGroupId) { return null; }
}
