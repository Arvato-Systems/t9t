/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.context;

import java.time.Instant;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.MessageStatisticsDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.util.JumpTool;

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
@Named("messageStatistics.ctx.showOkRequest")
public class ShowOkRequestContextMenuHandler implements IGridContextMenu<MessageStatisticsDTO> {
    @Override
    public boolean isEnabled(DataWithTracking<MessageStatisticsDTO, TrackingBase> dwt) {
        MessageStatisticsDTO dto = dwt.getData();
        return dto.getCountOk() > 0;
    }

    @Override
    public void selected(Grid28 lb, DataWithTracking<MessageStatisticsDTO, TrackingBase> dwt) {
        MessageStatisticsDTO dto = dwt.getData();

        AsciiFilter userIdFilter = new AsciiFilter(MessageDTO.meta$$userId.getName());
        userIdFilter.setEqualsValue(dto.getUserId());

        AsciiFilter pQONFilter = new AsciiFilter(MessageDTO.meta$$requestParameterPqon.getName());
        pQONFilter.setEqualsValue(dto.getRequestParameterPqon());

        InstantFilter executionStartedAtFilter  = new InstantFilter(MessageDTO.meta$$executionStartedAt.getName());
        final Instant fromInstant = dto.getSlotStart();
        final Instant toInstant = dto.getSlotStart().plusSeconds(3600L);
        executionStartedAtFilter.setLowerBound(fromInstant);
        executionStartedAtFilter.setUpperBound(toInstant);

        IntFilter returnCodeFilter = new IntFilter(MessageDTO.meta$$returnCode.getName());
        returnCodeFilter.setUpperBound(200000000);

        SearchFilter filter = SearchFilters.and(userIdFilter, SearchFilters.and(pQONFilter, SearchFilters.and(executionStartedAtFilter, returnCodeFilter)));

        JumpTool.jump("screens/monitoring/requests28.zul", filter, "screens/monitoring/messageStatistics.zul");
    }
}
