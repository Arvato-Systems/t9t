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
package com.arvatosystems.t9t.base.be.impl;

import java.util.Collections;
import java.util.List;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IExporterTool;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/** Utility class which performs the export or data return for SearchRequests which are composed from other requests, or provide
 * the data from in-memory sources.
 */
@Singleton
public class ExporterTool<DTO extends BonaPortable, TRACKING extends TrackingBase> implements IExporterTool<DTO, TRACKING> {

    @Override
    public Long storeAll(OutputSessionParameters op, List<DataWithTrackingW<DTO, TRACKING>> dataList) throws Exception {
        try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
            Long sinkRef = outputSession.open(op);
            if (outputSession.getUnwrapTracking(op.getUnwrapTracking())) {
                for (DataWithTrackingW<DTO, TRACKING> data : dataList) {
                    outputSession.store(data.getData());
                }
            } else {
                for (DataWithTrackingW<DTO, TRACKING> data : dataList) {
                    outputSession.store(data);
                }
            }
            // successful close: store ref
            return sinkRef;
        }
    }

    @Override
    public ReadAllResponse<DTO, TRACKING> returnOrExport(final List<DataWithTrackingW<DTO, TRACKING>> dataList, final OutputSessionParameters op) throws Exception {
        final ReadAllResponse<DTO, TRACKING> resp = new ReadAllResponse<DTO, TRACKING>();
        // if a searchOutputTarget has been defined, push the data into it, otherwise return the ReadAllResponse
        if (op == null) {
            resp.setDataList(dataList);
        } else {
            // push output into an outputSession (export it)
            op.setSmartMappingForDataWithTracking(Boolean.TRUE);
            resp.setSinkRef(storeAll(op, dataList));
            resp.setDataList(ImmutableList.<DataWithTrackingW<DTO,TRACKING>>of());
        }
        return resp;
    }

    @Override
    public <X> List<X> cut(List<X> dataList, int offset, int limit) {
        if (offset == 0 && limit == 0)
            return dataList;
        final int len = dataList.size();
        if (len <= offset) {
            // we skip at least as many entries as exist
            return Collections.emptyList();
        }
        if (limit == 0 || len <= offset + limit) {
            // no truncation of items, just skip the beginning
            return dataList.subList(offset, len);
        }
        // there is a limit, and we truncate at the end
        return dataList.subList(offset, offset + limit);
    }
}
