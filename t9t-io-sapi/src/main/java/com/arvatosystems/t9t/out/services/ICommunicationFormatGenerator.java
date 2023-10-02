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
package com.arvatosystems.t9t.out.services;

import java.io.IOException;
import java.nio.charset.Charset;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;

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
     * @throws IOException if there is an issue generating header data
     */
    void open(DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters, MediaXType effectiveType, FoldableParams fp, IOutputResource destination,
      Charset encoding, String tenantId) throws IOException;

    /**
     * Generate record data in a specific format.
     * @param recordNo source record no (counts records submitted by the application)
     * @param mappedRecordNo mapped record no (after preoutput transformer step)
     * @param recordId unique record id
     * @param record the record
     * @throws IOException if there is an issue generating record data
     */
    void generateData(int recordNo, int mappedRecordNo, long recordId, String partitionKey, String recordKey, BonaPortable record) throws IOException;

    /**
     * Generate footer datas in a specific format. Does not close the underlying OutputStream.
     * @throws IOException if there is an issue generating footer data
     */
    void close() throws IOException;

    /**
     * storeCustomElement() is supported for very few export formats only. It writes a special data object to the output stream.
     * This is currently supported for XML exports only.
     *
     * @param name the tag of the object
     * @param value the value
     *
     * @throws IOException
     */
    default <T> void storeCustomElement(String name, Class<T> valueClass, Object value) throws IOException {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Custom element for format " + this.getClass().getCanonicalName());
    }
}
