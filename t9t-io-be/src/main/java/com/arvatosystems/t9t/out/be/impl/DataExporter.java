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
package com.arvatosystems.t9t.out.be.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

/**
 * Component responsible for exporting given data to external output. For this purpose IOutputSession is used.
 *
 * This should only be used if the amount data is guaranteed to be very small, because everythign is done in memory.
 *
 * @author greg
 *
 */
public class DataExporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataExporter.class);

    /**
     * Exports given list of components implementing BonaPortable interface.
     *
     * @param data
     *            data to export
     * @param op
     *            parameters
     * @return id of created sink
     * @throws Exception
     */
    public final <DTO extends BonaPortable> Long exportData(List<DTO> data, OutputSessionParameters op) throws Exception {
        if (op == null) {
            return null;
        }

        // push output into an outputSession (export it)
        IOutputSession os = Jdp.getRequired(IOutputSession.class);
        Long sinkRef = null;
        try {
            sinkRef = os.open(op);
            for (DTO e : data) {
                os.store(e);
            }
            os.close();
        } catch (Exception e) {
            LOGGER.error("An error occurred on exporting data.", e);
            os.close(); // avoid resource leak
            throw e;
        }

        return sinkRef;
    }

    /**
     * Exports given list containing data elements and tracking information.
     *
     * @param data
     *            data to export
     * @param op
     *            parameters
     * @return id of created sink
     * @throws Exception
     */
    public final <DTO extends BonaPortable, TRACKING extends TrackingBase> Long exportDataWithTrackingW(List<DataWithTrackingW<DTO, TRACKING>> data,
            OutputSessionParameters op) throws Exception {
        if (op == null) {
            return null;
        }

        op.setSmartMappingForDataWithTracking(Boolean.TRUE);

        // push output into an outputSession (export it)
        IOutputSession os = Jdp.getRequired(IOutputSession.class);
        Long sinkRef = null;
        try {
            sinkRef = os.open(op);
            for (DataWithTrackingW<DTO, TRACKING> e : data) {
                os.store(e);
            }
            os.close();
        } catch (Exception e) {
            LOGGER.error("An error occurred on exporting data with tracking.", e);
            os.close(); // avoid resource leak
            throw e;
        }

        return sinkRef;
    }

}
