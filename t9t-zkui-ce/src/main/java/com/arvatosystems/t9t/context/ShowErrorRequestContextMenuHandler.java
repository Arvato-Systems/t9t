/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.context;

import java.time.Instant;
import java.time.ZoneId;

import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.components.tools.JumpTool;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.MessageStatisticsDTO;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("messageStatistics.ctx.showErrorRequest")
public class ShowErrorRequestContextMenuHandler implements IGridContextMenu<MessageStatisticsDTO> {
    @Override
    public boolean isEnabled(DataWithTracking<MessageStatisticsDTO, TrackingBase> dwt) {
        MessageStatisticsDTO dto = dwt.getData();
        return dto.getCountError() > 0;
    }

    @Override
    public void selected(Grid28 lb, DataWithTracking<MessageStatisticsDTO, TrackingBase> dwt) {
        MessageStatisticsDTO dto = dwt.getData();

        AsciiFilter userIdFilter = new AsciiFilter(MessageDTO.meta$$userId.getName());
        userIdFilter.setEqualsValue(dto.getUserId());

        AsciiFilter pQONFilter = new AsciiFilter(MessageDTO.meta$$requestParameterPqon.getName());
        pQONFilter.setEqualsValue(dto.getRequestParameterPqon());

        InstantFilter executionStartedAtFilter  = new InstantFilter(MessageDTO.meta$$executionStartedAt.getName());
        final Instant fromInstant = dto.getDay().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        final Instant toInstant = dto.getDay().plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        executionStartedAtFilter.setLowerBound(fromInstant);
        executionStartedAtFilter.setUpperBound(toInstant);

        IntFilter returnCodeFilter = new IntFilter(MessageDTO.meta$$returnCode.getName());
        returnCodeFilter.setLowerBound(200000000);

        SearchFilter filter = SearchFilters.and(userIdFilter, SearchFilters.and(pQONFilter, SearchFilters.and(executionStartedAtFilter, returnCodeFilter)));

        JumpTool.jump("screens/monitoring/requests28.zul", filter, "screens/monitoring/messageStatistics.zul");
    }
}
