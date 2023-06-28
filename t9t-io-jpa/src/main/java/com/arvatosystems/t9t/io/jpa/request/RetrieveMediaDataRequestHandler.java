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
package com.arvatosystems.t9t.io.jpa.request;

import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.RetrieveMediaDataRequest;
import com.arvatosystems.t9t.io.request.RetrieveMediaDataResponse;
import com.arvatosystems.t9t.io.services.IMediaDataSource;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ByteArray;

public class RetrieveMediaDataRequestHandler extends AbstractRequestHandler<RetrieveMediaDataRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveMediaDataRequestHandler.class);

    private final ISinkEntityResolver sinkResolver = Jdp.getRequired(ISinkEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RetrieveMediaDataRequest request) throws Exception {
        final SinkEntity mySinkEntity = sinkResolver.find(request.getSinkRef());
        if (mySinkEntity == null) {
            throw new ApplicationException(T9tException.RECORD_DOES_NOT_EXIST, "no Sink for Ref " + request.getSinkRef());
        }
        final RetrieveMediaDataResponse response = new RetrieveMediaDataResponse();
        // create a response with some default settings
        response.setReturnCode(0);
        // actual data requested. check if the data is downloadable (i.e. is a file)
        final IMediaDataSource srcHandler = Jdp.getOptional(IMediaDataSource.class, mySinkEntity.getCommTargetChannelType().name());

        if (srcHandler == null) {
            throw new T9tException(T9tIOException.OUTPUT_COMM_CHANNEL_NO_SRC_HANDLER, mySinkEntity.getCommTargetChannelType().name());
        }

        final String filePath = srcHandler.getAbsolutePath(mySinkEntity.getFileOrQueueName(), ctx);
        response.setReturnCode(T9tIOException.OUTPUT_COMM_CHANNEL_IO_ERROR);
        try (InputStream fis = srcHandler.open(filePath)) {
            // the file size is 16 MB max (i.e. can be returned in a single chunk: It should throw a new T9tIOException (FILE_TOO_BIG)
            if (fis.available() > T9tConstants.MAXIMUM_MESSAGE_LENGTH) {
                throw new T9tException(T9tIOException.FILE_TOO_BIG);
            }

            final byte[] buffer = new byte[T9tConstants.MAXIMUM_MESSAGE_LENGTH];
            int numRead = 0;
            // do a loop here, because an initial read may not return the full number of bytes
            while (numRead < T9tConstants.MAXIMUM_MESSAGE_LENGTH) {
                final int lastRead = fis.read(buffer, numRead, T9tConstants.MAXIMUM_MESSAGE_LENGTH - numRead);
                LOGGER.trace("read returned {} bytes", lastRead);
                if (lastRead < 0) {
                    break; // EOF
                }
                numRead += lastRead;
            }
            LOGGER.debug("received {} bytes in total for {}", numRead, filePath);
            final MediaData mediaData = new MediaData();
            mediaData.setMediaType(mySinkEntity.getCommFormatType());
            final MediaTypeDescriptor description = MediaTypeInfo.getFormatByType(mySinkEntity.getCommFormatType());
            if (description.getIsText()) {
                mediaData.setText(new ByteArray(buffer, 0, numRead).asString());
            } else {
                mediaData.setRawData(new ByteArray(buffer, 0, numRead));
            }
            response.setMediaData(mediaData);
            response.setReturnCode(0);
        } catch (final FileNotFoundException e) {
            LOGGER.error("{} resource {} was not found", mySinkEntity.getCommTargetChannelType().name(), filePath);
            throw new T9tException(T9tException.FILE_NOT_FOUND_FOR_DOWNLOAD, filePath);
        }

        return response;
    }
}
