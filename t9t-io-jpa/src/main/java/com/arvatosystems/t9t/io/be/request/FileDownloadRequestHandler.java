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

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.ISinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.FileDownloadRequest;
import com.arvatosystems.t9t.io.request.FileDownloadResponse;
import com.arvatosystems.t9t.io.services.IMediaDataSource;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteArray;

public class FileDownloadRequestHandler extends AbstractRequestHandler<FileDownloadRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadRequestHandler.class);

    private final ISinkEntityResolver sinkResolver = Jdp.getRequired(ISinkEntityResolver.class);
    private final ISinkDTOMapper sinksMapper = Jdp.getRequired(ISinkDTOMapper.class);

    @Override
    public FileDownloadResponse execute(RequestContext ctx, FileDownloadRequest request) throws Exception {
        SinkEntity mySinkEntity = sinkResolver.find(request.getSinkRef());
        if (mySinkEntity == null) {
            throw new ApplicationException(T9tException.RECORD_DOES_NOT_EXIST, "no Sink for Ref " + request.getSinkRef());
        }
        FileDownloadResponse response = new FileDownloadResponse();
        SinkDTO mySink = sinksMapper.mapToDto(mySinkEntity);
        // create a response with some default settings
        response.setSink(mySink);
        response.setReturnCode(0);
        response.setHasMore(false);
        response.setData(ByteArray.ZERO_BYTE_ARRAY);
        // actual data requested. check if the data is downloadable (i.e. is a file)
        final IMediaDataSource srcHandler = Jdp.getOptional(IMediaDataSource.class, mySinkEntity.getCommTargetChannelType().name());

        if (srcHandler == null) {
            throw new T9tException(T9tIOException.OUTPUT_COMM_CHANNEL_NO_SRC_HANDLER, mySinkEntity.getCommTargetChannelType().name());
        }

        String filePath = srcHandler.getAbsolutePath(mySinkEntity.getFileOrQueueName(), ctx);
        response.setReturnCode(T9tIOException.OUTPUT_COMM_CHANNEL_IO_ERROR);
        try (InputStream fis = srcHandler.open(filePath)) {
            fis.skip(request.getOffset());
            int myMaxsize = request.getLimit();
            if (myMaxsize == 0 || myMaxsize > T9tConstants.MAXIMUM_MESSAGE_LENGTH) {
                myMaxsize = T9tConstants.MAXIMUM_MESSAGE_LENGTH;
            }
            byte[] buffer = new byte[myMaxsize];
            int numRead = 0;
            // do a loop here, because an initial read may not return the full number of bytes
            while (numRead < myMaxsize) {
                int lastRead = fis.read(buffer, numRead, myMaxsize - numRead);
                LOGGER.trace("read returned {} bytes", lastRead);
                if (lastRead < 0) {
                    break; // EOF
                }
                numRead += lastRead;
            }
            boolean hasMore = srcHandler.hasMore(fis);
            LOGGER.debug("received {} bytes in total for {}, hasMore = {}", numRead, filePath, hasMore);
            // transfer data to response
            response.setHasMore(hasMore);
            if (!hasMore) {
                //if whole file is completed, the lastDownloadTimestamp is set to now
                updateLastDownloadTimestamp(mySinkEntity);
            }
            response.setData(new ByteArray(buffer, 0, numRead));
            response.setReturnCode(0);
        } catch (FileNotFoundException e) {
            LOGGER.error("{} resource {} was not found", mySinkEntity.getCommTargetChannelType().name(), filePath);
            throw new T9tException(T9tException.FILE_NOT_FOUND_FOR_DOWNLOAD, filePath);
        }
        return response;
    }

    private void updateLastDownloadTimestamp(SinkEntity mySinkEntity) {
        mySinkEntity.setLastDownloadTimestamp(new Instant());
        sinkResolver.update(mySinkEntity);
        sinkResolver.flush();
    }
}
