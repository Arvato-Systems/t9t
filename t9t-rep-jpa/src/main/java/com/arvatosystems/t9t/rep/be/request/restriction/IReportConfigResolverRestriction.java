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
package com.arvatosystems.t9t.rep.be.request.restriction;

import java.util.List;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.IResolverRestriction;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.rep.ReportConfigRef;
import com.arvatosystems.t9t.rep.jpa.entities.ReportConfigEntity;


/**
 * ReportConfigResolverRestriction handles backend checking for reportConfigId permission.
 * @author RREN001
 *
 */
public interface IReportConfigResolverRestriction extends IResolverRestriction<ReportConfigRef, FullTrackingWithVersion, ReportConfigEntity> {

    /** get all of the permissionId that's allowed for the user */
    List<String> getPermissionIdList(RequestContext ctx);
}
