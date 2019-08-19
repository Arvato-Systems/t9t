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
package com.arvatosystems.t9t.out.be;

import java.io.IOException;
import java.nio.charset.Charset;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.be.impl.output.FoldableParams;
import com.arvatosystems.t9t.out.services.IOutputResource;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.util.ApplicationException;

/**
 * Communication format generators convert structured records into byte or character streams.
 * Implementations are responsible for pushing the data into the destination.
 *
 */
public interface ICommunicationFormatGenerator {

    /**
     * Generate header datas in a specific format.
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @throws IOException, ApplicationException if there is an issue generating header data
     */
    void open(DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters, MediaXType effectiveType, FoldableParams fp, IOutputResource destination, Charset encoding,
            String tenantId) throws IOException, ApplicationException;

    /**
     * Generate record datas in a specific format.
     * @param recordNo source record no (counts records submitted by the application)
     * @param mappedRecordNo mapped record no (after preoutput transformer step)
     * @param recordId unique record id
     * @param record the record
     * @throws IOException, ApplicationException if there is an issue generating record data
     */
    void generateData(int recordNo, int mappedRecordNo, long recordId, BonaPortable record) throws IOException, ApplicationException;

    /**
     * Generate footer datas in a specific format. Does not close the underlying OutputStream.
     * @throws IOException, ApplicationException if there is an issue generating footer data
     */
    void close() throws IOException, ApplicationException;
}
