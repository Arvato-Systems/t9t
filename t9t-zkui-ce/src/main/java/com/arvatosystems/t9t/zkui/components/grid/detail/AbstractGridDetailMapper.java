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
package com.arvatosystems.t9t.zkui.components.grid.detail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

public abstract class AbstractGridDetailMapper<MAIN extends BonaPortable, DETAIL extends BonaPortable>
    implements IGridDetailMapper<DataWithTrackingS<MAIN, TrackingBase>, DataWithTrackingS<DETAIL, TrackingBase>> {

    @Override
    public List<DataWithTrackingS<DETAIL, TrackingBase>> mapDetails(final DataWithTrackingS<MAIN, TrackingBase> mainDto) {
        final List<DETAIL> details = getDetails(mainDto.getData());
        if (details != null) {
            final List<DataWithTrackingS<DETAIL, TrackingBase>> dwtList = new ArrayList<>(details.size());
            for (DETAIL detailDto : details) {
                final DataWithTrackingS<DETAIL, TrackingBase> dwt = new DataWithTrackingS<>(detailDto, getTracking(mainDto, detailDto),
                    getTenant(mainDto, detailDto));
                dwtList.add(dwt);
            }
            return dwtList;
        }
        return Collections.emptyList();
    }

    protected abstract List<DETAIL> getDetails(MAIN mainDto);

    protected TrackingBase getTracking(final DataWithTrackingS<MAIN, TrackingBase> mainDto, final DETAIL detailDto) {
        return new NoTracking();
    }

    protected String getTenant(final DataWithTrackingS<MAIN, TrackingBase> mainDto, final DETAIL detailDto) {
        return mainDto.getTenantId();
    }
}
