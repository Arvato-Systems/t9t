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
package com.arvatosystems.t9t.io.be.request;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.request.DataSinkTestRequest;
import com.arvatosystems.t9t.io.request.DemoRecord;

import de.jpaw.dp.Jdp;

public class DataSinkTestRequestHandler extends AbstractRequestHandler<DataSinkTestRequest> {

    @Override
    public ServiceResponse execute(DataSinkTestRequest request) throws Exception {
        // Obtain a session first
        OutputSessionParameters sessionParams = new OutputSessionParameters();
        sessionParams.setDataSinkId(request.getDataSinkId());

        DemoRecord r = new DemoRecord(0, new BigDecimal("-3.14"), "Hello, world", LocalDate.now(), LocalDateTime.now(), null, true, false);
        Long sinkRef = null;

        try (IOutputSession os = Jdp.getRequired(IOutputSession.class)) {

            // create a new instance of an output session
            // open the session (creates the file, if using files)
            sinkRef = os.open(sessionParams); // result not needed here

            // write all the records
            for (int i = 0; i < request.getNumDataRecords(); ++i) {
                r.setRecordNo(i);
                os.store(r);
            }
//        } finally {
//            os.close();
        }
        SinkCreatedResponse response = new SinkCreatedResponse();
        response.setReturnCode(0);
        response.setSinkRef(sinkRef);
        return response;
    }

}
