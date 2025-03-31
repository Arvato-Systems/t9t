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
package com.arvatosystems.t9t.mfcobol.in.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.in.be.impl.AbstractBufferedFormatConverter;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.mfcobol.in.IRecordTypeSelector;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.mfcobol.MfcobolParser;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;

/**
 * Format converter for binary formats with a fixed record length.
 *
 * The record length is obtained as a property of the target DTO.
 */
@Named("MFCOBOL")
@Dependent
public class MfcobolFormatConverter extends AbstractBufferedFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(MfcobolFormatConverter.class);

    private static final int DEFAULT_VAR_HEADER_SIZE        = 2;            // for files with records <= 4095 bytes
    private static final int DEFAULT_VAR_ALIGNMENT_SIZE     = 1;            // determines padding
    private static final int MAX_RECORD_SIZE_SMALL_HEADER   = 4095;         // format specific limit
    private static final int MAX_RECORD_SIZE_BIG_HEADER     = 1024 * 1024;  // our limit (tunable)

    protected int recordSize = 0;
    protected boolean fixedRecordLength = false;
    protected int fixedRecordTerminatorSize = 2;
    protected int varHeaderSize = DEFAULT_VAR_HEADER_SIZE;
    protected int varAlignmentSize = DEFAULT_VAR_ALIGNMENT_SIZE;
    protected IRecordTypeSelector formatSelector = null;

    @Override
    public void open(final IInputSession newInputSession, final Map<String, Object> newParams, final BonaPortableClass<?> newBaseBClass) {
        super.open(newInputSession, newParams, newBaseBClass);

        // determine record size: either from data sink, or from format description
        if (importDataSinkDTO.getRecordSize() != null) {
            recordSize = importDataSinkDTO.getRecordSize();
        }
        // possibly override with setting in record descriptor
        final String recordSizeString = baseBClass.getProperty("recordSize");
        if (!T9tUtil.isBlank(recordSizeString)) {
            recordSize = Integer.valueOf(recordSizeString);
        }

        // genericParameter1 determines if we have fixed format or one of the IDX-formats
        // genericParameter2 determines if we use big record sizes or not (for IDX files)
        // files with headers are not yet supported
        switch (T9tUtil.nvl(importDataSinkDTO.getGenericParameter1(), "FFRF-CRLF")) {
        case "FFRF-CRLF":  // fixed format relative file
            setFixedAndRequireRecordSize();
            fixedRecordTerminatorSize = 2;  // CR + LF
            break;
        case "FFRF-LF":  // UNIX
            setFixedAndRequireRecordSize();
            fixedRecordTerminatorSize = 1;  // LF
            break;
        case "IDX1":
        case "IDX2":
            setIdxFormatAndEvaluateHeaderSize();
            varAlignmentSize = 1;
            break;
        case "IDX3":
        case "IDX4":
            setIdxFormatAndEvaluateHeaderSize();
            varAlignmentSize = 4;
            break;
        case "IDX8":
        case "IDX12":
            setIdxFormatAndEvaluateHeaderSize();
            varAlignmentSize = 8;
            break;
        default:
            LOGGER.error("Unsupported genericParameter1 {} for data sink {}", importDataSinkDTO.getGenericParameter1(), importDataSinkDTO.getDataSinkId());
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED);
        }

        // finally, determine a possible selector for subtypes
        formatSelector = Jdp.getOptional(IRecordTypeSelector.class, baseBClass.getPqon());
    }

    protected void setFixedAndRequireRecordSize() {
        fixedRecordLength = true;
        if (recordSize <= 2) {
            LOGGER.error("DataSink {} for PQON {} does not specify a record size", importDataSinkDTO.getDataSinkId(), baseBClass.getPqon());
            throw new T9tException(T9tException.MISSING_CONFIGURATION, importDataSinkDTO.getDataSinkId());
        }
    }
    protected void setIdxFormatAndEvaluateHeaderSize() {
        fixedRecordLength = false;
        if ("4".equals(importDataSinkDTO.getGenericParameter2())) {
            varHeaderSize = 4;
        }
    }

    protected int recordCounter = 0;  // number of undeleted records
    protected int deletedRecords = 0; // number of deleted entries
    protected int otherRecords = 0;   // other records (only IDX)

    @Override
    public void processBuffered(final InputStream is) {
        try {
            final byte[] buffer = new byte[fixedRecordLength
                    ? recordSize + fixedRecordTerminatorSize
                    : varHeaderSize == 2 ? MAX_RECORD_SIZE_SMALL_HEADER : MAX_RECORD_SIZE_BIG_HEADER];
            for (;;) {
                if (fixedRecordLength) {
                    // simple processing
                    final int nowRead = is.read(buffer, 0, recordSize + fixedRecordTerminatorSize);
                    if (nowRead < recordSize + fixedRecordTerminatorSize) {
                        // end of file assumed
                        if (nowRead > 0) {
                            LOGGER.warn("Last record had partial data: {} bytes instead of {}", nowRead, recordSize);
                        }
                        break;
                    }
                    if (buffer[recordSize + fixedRecordTerminatorSize - 1] != '\n') {
                        ++deletedRecords;
                    } else {
                        ++recordCounter;
                        processSingleRecord(buffer, recordSize);
                    }
                } else {
                    // variable record lengths
                    final int readHeader = is.read(buffer, 0, varHeaderSize);
                    if (readHeader < varHeaderSize) {
                        // end of file assumed
                        if (readHeader > 0) {
                            LOGGER.warn("Last record had partial data: {} bytes instead of {} reading HEADER", varHeaderSize, recordSize);
                        }
                        break;
                    }
                    // obtain length, and info if deleted or not
                    final int recordType = (int)(buffer[0] >> 4);
                    final int recordSize0 = (((int)(buffer[0]) & 0x0f) << 8) | ((int)(buffer[1]) & 0xff);
                    final int recordSizeEff = varHeaderSize == 2 ? recordSize0
                            : (recordSize0 << 16) | (((int)(buffer[2]) & 0xff) << 8) | ((int)(buffer[3]) & 0xff);

                    // determine any filler
                    final int residual = (varHeaderSize + recordSizeEff) % varAlignmentSize;
                    final int paddingBytes = residual == 0 ? 0 : varAlignmentSize - residual;
                    final int bytesToRead = recordSizeEff + paddingBytes;

                    LOGGER.debug("Header is {}, record Size {}, total bytes of entry {}", recordType, recordSize, bytesToRead + varHeaderSize);

                    // read the data
                    final int nowRead = is.read(buffer, 0, bytesToRead);
                    if (nowRead < bytesToRead) {
                        // end of file assumed
                        if (bytesToRead > 0) {
                            LOGGER.warn("Last record had partial data: {} bytes instead of {}", nowRead, bytesToRead);
                        }
                        break;
                    }

                    switch (recordType) {
                    case 2:
                        ++deletedRecords;
                        break;
                    case 4:
                        processSingleRecord(buffer, recordSize);
                        ++recordCounter;
                        break;
                    default:
                        ++otherRecords;
                    }
                }
            }
            LOGGER.info("Processed file for DataSink {}: {} data records, {} deleted, {} other, record type selector was {}",
                    importDataSinkDTO.getDataSinkId(), recordCounter, deletedRecords, otherRecords,
                    formatSelector == null ? "NULL" : formatSelector.getClass().getCanonicalName());
        } catch (final IOException e) {
            LOGGER.error("Error when reading line from input stream.", e);
            throw new T9tException(T9tIOException.IO_EXCEPTION);
        } finally {
            try {
                is.close();
            } catch (final IOException f) {
                // should (hopefully) never happen because its already caught beforehand.
                LOGGER.warn("Error when closing InputStream", f);
            }
        }
    }

    protected void processSingleRecord(final byte[] buffer, final int dataSize) {
        try {
            final MfcobolParser cp = new MfcobolParser(buffer, 0, dataSize, importCharset);

            // final BonaPortable resultObject = cp.readObject(baseBClass.getMetaData().meta$$this, baseBClass.getBonaPortableClass());
            final BonaPortable resultObject = baseBClass.newInstance();
            cp.setClassName(baseBClass.getPqon());
            resultObject.deserialize(cp);

            if (formatSelector == null) {
                // single record type
                inputSession.process(resultObject);
            } else {
                // initial parse was just a delegation key
                final BonaPortableClass<?> detailedBclass = formatSelector.evaluateSelector(resultObject);
                if (detailedBclass != null) {
                    cp.resetParseIndex(0);
                    final BonaPortable resultObject2 = detailedBclass.newInstance();
                    cp.setClassName(detailedBclass.getPqon());
                    resultObject2.deserialize(cp);
                    inputSession.process(resultObject2);
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception caught, parsing record: ", e);
        }
    }
}
