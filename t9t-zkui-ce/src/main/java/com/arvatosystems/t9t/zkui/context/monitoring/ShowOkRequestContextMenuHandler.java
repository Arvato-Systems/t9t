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
package com.arvatosystems.t9t.zkui.context.monitoring;

import java.util.List;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.InstantFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.StringFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.MessageStatisticsDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.util.JumpTool;

@Singleton
@Named("messageStatistics.ctx.showOkRequest")
public class ShowOkRequestContextMenuHandler implements IGridContextMenu<MessageStatisticsDTO> {
    @Override
    public boolean isEnabled(final DataWithTracking<MessageStatisticsDTO, TrackingBase> dwt) {
        MessageStatisticsDTO dto = dwt.getData();
        return dto.getCountOk() > 0;
    }

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<MessageStatisticsDTO, TrackingBase> dwt) {
        final MessageStatisticsDTO dto = dwt.getData();

        final StringFilter userIdFilter = SearchFilters.equalsFilter(MessageDTO.meta$$userId.getName(), dto.getUserId());
        final StringFilter pQONFilter = SearchFilters.equalsFilter(MessageDTO.meta$$requestParameterPqon.getName(), dto.getRequestParameterPqon());
        final InstantFilter executionStartedAtFilter  = SearchFilters.rangeFilter(MessageDTO.meta$$executionStartedAt.getName(), dto.getSlotStart(), dto.getSlotStart().plusSeconds(3600L));
        final IntFilter returnCodeFilter = SearchFilters.rangeFilter(MessageDTO.meta$$returnCode.getName(), null, 200000000);

        final SearchFilter filter = SearchFilters.and(List.of(userIdFilter, pQONFilter, executionStartedAtFilter, returnCodeFilter));

        JumpTool.jump("screens/monitoring/requests28.zul", filter, getBackNaviLink());
    }

    protected String getBackNaviLink() {
        return "screens/monitoring/messageStatistics.zul";
    }
}
