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
