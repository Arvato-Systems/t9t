/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.be.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.T9tIOException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ExceptionUtil;

/**
 * Binary format converter which allows import of arbitrary binary data.
 * It is up to the configured IInputDataTransformer to create a valid request. (e.g. an file upload request.)
 */
@Dependent
@Named("COMPACT_BONAPARTE")
public class CompactBonaparteFormatConverter extends AbstractInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompactBonaparteFormatConverter.class);

    protected int maxSize = 65000;

    @Override
    public void open(final IInputSession inputSession, final Map<String, Object> params, final BonaPortableClass<?> baseBClass) {
        super.open(inputSession, params, baseBClass);
        if (importDataSinkDTO.getRecordSize() != null && importDataSinkDTO.getRecordSize() > 10) {
            maxSize = importDataSinkDTO.getRecordSize();
        }
    }

    @Override
    public void process(final InputStream is) {
        // convert the inputStream to byte array
        try {
            final ByteArray ba = ByteArray.fromInputStream(is, maxSize);
            final BonaPortable obj = new CompactByteArrayParser(ba.getBytes(), 0, -1).readRecord();
            inputSession.process(obj);
        } catch (final IOException e) {
            LOGGER.error("IOException {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tIOException.IO_EXCEPTION, ExceptionUtil.causeChain(e));
        }
    }

    @Override
    public void process(final byte[] data) {
        final BonaPortable obj = new CompactByteArrayParser(data, 0, -1).readRecord();
        inputSession.process(obj);
    }
}
