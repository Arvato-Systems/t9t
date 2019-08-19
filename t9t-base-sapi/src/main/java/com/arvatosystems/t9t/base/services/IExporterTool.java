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
package com.arvatosystems.t9t.base.services;

import java.util.List;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;

public interface IExporterTool<DTO extends BonaPortable, TRACKING extends TrackingBase> {
    /** Opens an output session, pushes all data into it, and returns the generated sinkRef.
     * @throws Exception */
    Long storeAll(OutputSessionParameters op, List<DataWithTrackingW<DTO, TRACKING>> dataList) throws Exception;

    ReadAllResponse<DTO, TRACKING> returnOrExport(List<DataWithTrackingW<DTO, TRACKING>> dataList, OutputSessionParameters op) throws Exception;

    /** Return a sublist which respects offset and limit. */
    <X> List<X> cut(List<X> dataList, int offset, int limit);
}
