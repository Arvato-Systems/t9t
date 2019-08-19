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
package com.arvatosystems.t9t.out.be.impl.output;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.AbstractExportRequest;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.IOutputSessionExporter;
import com.arvatosystems.t9t.io.T9tIOException;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class OutputSessionExporter implements IOutputSessionExporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputSessionExporter.class);
    protected final Integer DEFAULT_CHUNK_SIZE            = 200;
    protected final Integer DEFAULT_MAX_NUMBER_OF_RECORDS = Integer.MAX_VALUE;
    protected final Provider<IOutputSession> osp = Jdp.getProvider(IOutputSession.class);

    @Override
    public <D> SinkCreatedResponse runExport(
        final AbstractExportRequest request,
        final String defaultDataSinkId,
        final Map<String, Object> params,  // optional
        final BiFunction<Long, Integer, List<D>> chunkReader,
        final BiFunction<List<D>, IOutputSession, Long> chunkWriter,  // alternative to writer
        final BiFunction<D, IOutputSession, Long> writer
      ) {
        final IOutputSession outputSession = osp.get();
        final OutputSessionParameters sessionParams = new OutputSessionParameters();
        sessionParams.setDataSinkId(request.getDataSinkId() == null ? defaultDataSinkId : request.getDataSinkId());
        sessionParams.setAdditionalParameters(params);
        final Long targetSinkRef = outputSession.open(sessionParams);

        // determine how many records to write in one go
        Integer chunkSize = request.getChunkSize();
        if (chunkSize == null)
            chunkSize = outputSession.getChunkSize();
        if (chunkSize == null)
            chunkSize = DEFAULT_CHUNK_SIZE;

        // determine how many records to write in total
        Integer maxNumberOfRecords = request.getMaxNumberOfRecords();
        if (maxNumberOfRecords == null)
            maxNumberOfRecords = outputSession.getMaxNumberOfRecords();
        if (maxNumberOfRecords == null)
            maxNumberOfRecords = DEFAULT_MAX_NUMBER_OF_RECORDS;

        LOGGER.info("Starting export on dataSinkId {} with chunk size {} and {} max records",
            sessionParams.getDataSinkId(), chunkSize, maxNumberOfRecords);
        int recordsDone = 0;

        Long lastRecordDone = Long.valueOf(0L);
        while (recordsDone < maxNumberOfRecords) {
            int maxRecordsToBeReadNow = maxNumberOfRecords.intValue() - recordsDone;
            if (maxRecordsToBeReadNow > chunkSize)
                maxRecordsToBeReadNow = chunkSize;
            final List<D> chunk = chunkReader.apply(lastRecordDone, maxRecordsToBeReadNow);
            LOGGER.debug("Export on {}: read chunk size of {} for max {}, start key {}",
                    sessionParams.getDataSinkId(), chunk.size(), maxRecordsToBeReadNow, lastRecordDone);
            if (chunk.isEmpty())
                break;  // no more data
            recordsDone += chunk.size();
            if (chunkWriter != null) {
                lastRecordDone = chunkWriter.apply(chunk, outputSession);
            } else {
                for (D record : chunk) {
                    lastRecordDone = writer.apply(record, outputSession);
                }
            }
            if (chunk.size() < maxRecordsToBeReadNow) {
                // did not get full list: there will be no more records (or we are not interested)
                break;
            }
        }
        try {
            outputSession.close();
        } catch (Exception e) {
            LOGGER.error("Could not close data sink: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tIOException.IO_EXCEPTION, "Could not close", e);
        }

        // response
        final SinkCreatedResponse response = new SinkCreatedResponse();
        response.setSinkRef(targetSinkRef);
        response.setNumResults((long)recordsDone);
        return response;
    }
}
