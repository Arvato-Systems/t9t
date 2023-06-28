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
package com.arvatosystems.t9t.out.be.impl;

import java.io.OutputStream;
import java.util.HashMap;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;

/**
 * Proxy to class outputSession, but this one will not be found using Jdp, but rather
 * by invoking a specific constructor with parameter: maximumNumberOfRecordsPerFile.
 */
public class SplittingOutputSession implements IOutputSession {
    private final int maximumNumberOfRecordsPerFile;   // 0 = unlimited
    private int currentRecordCountInFile = 0;
    private int part = 0;
    private IOutputSession os = Jdp.getRequired(IOutputSession.class);  // current instance of OutputSession (one is valid at any point in time).
    private OutputSessionParameters params;

    public SplittingOutputSession(final int maximumNumberOfRecordsPerFile) {
        this.maximumNumberOfRecordsPerFile = maximumNumberOfRecordsPerFile;
    }

    private void switchFileIfRowCountReached() {
        if (maximumNumberOfRecordsPerFile > 0) {
            ++currentRecordCountInFile;
            if (currentRecordCountInFile > maximumNumberOfRecordsPerFile) {
                switchFile();
                currentRecordCountInFile = 1;
            }
        }
    }

    private Long switchFile() {
        if (part > 0) {
            // close the current one first
            try {
                os.close();
            } catch (final Exception e) {
                // ignore (for now)
            }
            // create a new instance of an IOutputSession
            os = Jdp.getRequired(IOutputSession.class);
        }
        ++part;
        // store file number (part) as additional parameter
        params.getAdditionalParameters().put("partNo", part);
        return os.open(params);
    }

    @Override
    public Long open(final OutputSessionParameters myParams) {
        // make sure there is a modifiable map of parameters
        if (myParams.getAdditionalParameters() == null) {
            myParams.setAdditionalParameters(new HashMap<>());
        } else {
            // defensive copy, modifiable
            myParams.setAdditionalParameters(new HashMap<>(myParams.getAdditionalParameters()));
        }
        this.params = myParams;
        return switchFile();  // caveat: this is no longer a unique sinkRef
    }

    @Override
    public OutputStream getOutputStream() {
        if (maximumNumberOfRecordsPerFile != 0) {
            throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, "Cannot stream to a split OutputSession");
        }
        return os.getOutputStream();
    }

    @Override
    public MediaXType getCommunicationFormatType() {
        return os.getCommunicationFormatType();
    }

    @Override
    public void store(final BonaPortable record) {
        switchFileIfRowCountReached();
        os.store(record);
    }

    @Override
    public void store(final Long recordRef, final BonaPortable record) {
        switchFileIfRowCountReached();
        os.store(recordRef, record);
    }

    @Override
    public void store(final Long recordRef, final String partitionKey, final String recordKey, final BonaPortable record) {
        switchFileIfRowCountReached();
        os.store(recordRef, partitionKey, recordKey, record);
    }

    @Override
    public void close() throws Exception {
        os.close();
    }

    @Override
    public Integer getMaxNumberOfRecords() {
        return os.getMaxNumberOfRecords();
    }

    @Override
    public Integer getChunkSize() {
        return os.getChunkSize();
    }

    @Override
    public String getFileOrQueueName() {
        return os.getFileOrQueueName();  // caveat: this is no longer a unique sinkRef
    }

    @Override
    public boolean getUnwrapTracking(final Boolean ospSetting) {
        return os.getUnwrapTracking(ospSetting);
    }

    @Override
    public Object getZ(final String key) {
        return os.getZ(key);
    }

    @Override
    public void storeCustomElement(String name, Object value) {
        os.storeCustomElement(name, value);
    }
}
