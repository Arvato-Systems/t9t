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
package com.arvatosystems.t9t.base.be.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IExporterTool2;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.ISplittingOutputSessionProvider;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/** Utility class which performs the export or data return for SearchRequests which are composed from other requests, with DTO extensions / post processing.
 */
@Singleton
public class ExporterTool2<DTO extends BonaPortable, EXTDTO extends DTO, TRACKING extends TrackingBase> implements IExporterTool2<DTO, EXTDTO, TRACKING> {
    protected final ISplittingOutputSessionProvider splittingOutputSessionProvider = Jdp.getRequired(ISplittingOutputSessionProvider.class);

    @Override
    public Long storeAll(final OutputSessionParameters op, final List<DataWithTrackingS<DTO, TRACKING>> dataList, final Integer maxRecords,
      final Function<DTO, EXTDTO> converter) throws Exception {
        try (IOutputSession outputSession = splittingOutputSessionProvider.get(maxRecords)) {
            final Long sinkRef = outputSession.open(op);
            for (final DataWithTrackingS<DTO, TRACKING> data : dataList) {
                final EXTDTO extDto = converter.apply(data.getData());
                if (extDto != null) {
                    data.setData(extDto);  // replace the data with the extended one
                }
                outputSession.store(data);
            }
            // successful close: store ref
            return sinkRef;
        }
    }

    @Override
    public ReadAllResponse<EXTDTO, TRACKING> returnOrExport(
      final List<DataWithTrackingS<DTO, TRACKING>> dataList, final OutputSessionParameters op, final Function<DTO, EXTDTO> converter) throws Exception {
        final ReadAllResponse<EXTDTO, TRACKING> resp = new ReadAllResponse<>();
        // if a searchOutputTarget has been defined, push the data into it, otherwise return the ReadAllResponse
        if (op == null) {
            // conversion of the data list is required
            final List<DataWithTrackingS<EXTDTO, TRACKING>> extDataList = new ArrayList<>(dataList.size());
            for (final DataWithTrackingS<DTO, TRACKING> data : dataList) {
                final EXTDTO extDto = converter.apply(data.getData());
                if (extDto != null) {
                    data.setData(extDto);  // replace the data with the extended one
                    // do a cast instead of new allocation, because that supports extensions of DataWithTrackingS as well
                    extDataList.add((DataWithTrackingS<EXTDTO, TRACKING>)data);
                }
            }
            resp.setDataList(extDataList);
        } else {
            // push output into an outputSession (export it)
            op.setSmartMappingForDataWithTracking(Boolean.TRUE);
            resp.setSinkRef(storeAll(op, dataList, null, converter));
            resp.setDataList(ImmutableList.<DataWithTrackingS<EXTDTO, TRACKING>>of());
        }
        return resp;
    }
}
