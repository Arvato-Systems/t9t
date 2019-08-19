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
package com.arvatosystems.t9t.rep.jpa.impl;

import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportConfigRef;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.ReportParamsRef;
import com.arvatosystems.t9t.rep.be.request.restriction.IReportConfigResolverRestriction;
import com.arvatosystems.t9t.rep.jpa.mapping.IReportConfigDTOMapper;
import com.arvatosystems.t9t.rep.jpa.mapping.IReportParamsDTOMapper;
import com.arvatosystems.t9t.rep.jpa.persistence.IReportConfigEntityResolver;
import com.arvatosystems.t9t.rep.jpa.persistence.IReportParamsEntityResolver;
import com.arvatosystems.t9t.rep.services.IRepPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class RepPersistenceAccess implements IRepPersistenceAccess {

    private final IReportParamsEntityResolver reportParamsResolver = Jdp.getRequired(IReportParamsEntityResolver.class);
    private final IReportParamsDTOMapper      reportParamsMapper   = Jdp.getRequired(IReportParamsDTOMapper.class);
    private final IReportConfigEntityResolver reportConfigResolver = Jdp.getRequired(IReportConfigEntityResolver.class);
    private final IReportConfigDTOMapper      reportConfigMapper   = Jdp.getRequired(IReportConfigDTOMapper.class);
    private final IReportConfigResolverRestriction reportConfigResolverRestriction = Jdp.getRequired(IReportConfigResolverRestriction.class);

    @Override
    public ReportConfigDTO getConfigDTO(ReportConfigRef configRef) throws Exception {
        return reportConfigMapper.mapToDto(
                reportConfigResolverRestriction.apply(reportConfigResolver.getEntityData(configRef, true)));
    }

    @Override
    public ReportParamsDTO getParamsDTO(ReportParamsRef paramsRef) throws Exception {
        return reportParamsMapper.mapToDto(reportParamsResolver.getEntityData(paramsRef, true));
    }

}
