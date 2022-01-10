/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.request.WriteRecordsToDataSinkRequest;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;

/**
 * An implementation of a file export service request.
 */
public class WriteRecordsToDataSinkRequestHandler extends AbstractRequestHandler<WriteRecordsToDataSinkRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteRecordsToDataSinkRequestHandler.class);

    @Override
    public SinkCreatedResponse execute(final RequestContext ctx, final WriteRecordsToDataSinkRequest rq) throws Exception {
        Long sinkRef;
        long count = 0L;

        // create outputSessionParameters
        final OutputSessionParameters osp = new OutputSessionParameters();
        osp.setDataSinkId(rq.getDataSinkId());

        try (IOutputSession os = Jdp.getRequired(IOutputSession.class)) {
            // open the session (creates the file, if using files)
            sinkRef = os.open(osp);
            if (rq.getRecords1() != null) {
                count += rq.getRecords1().size();
                for (final BonaPortable o: rq.getRecords1()) {
                    os.store(o);
                }
            }
            if (rq.getRecords2() != null) {
                count += rq.getRecords2().size();
                for (final BonaPortable o: rq.getRecords2()) {
                    os.store(o);
                }
            }
        } catch (final Exception e) {
            // In case of any error returnCode <> 0.
            LOGGER.error("Exception occured during file storage (OutputSession) - Error Message {} ", e);
            throw new T9tException(T9tException.GENERAL_EXCEPTION, e.getMessage());
        }
        final SinkCreatedResponse response = new SinkCreatedResponse();
        response.setReturnCode(0);
        response.setSinkRef(sinkRef);
        response.setNumResults(count);
        return response;
    }
}
