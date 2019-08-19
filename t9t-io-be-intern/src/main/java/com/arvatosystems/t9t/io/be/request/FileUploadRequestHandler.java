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

import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.request.FileUploadRequest;

import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;

/**
 * An example implementation of a file export service request. For simplicity, we inherit from an existing method which provides the required data. Rev. 2,
 * using try with resources.
 */
public class FileUploadRequestHandler extends AbstractRequestHandler<FileUploadRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadRequestHandler.class);

    @Override
    public ServiceResponse execute(FileUploadRequest params) throws Exception {
        Long sinkRef;
        MediaXType communicationFormatType = params.getParameters().getCommunicationFormatType();

        if ((communicationFormatType == null) || (communicationFormatType.getBaseEnum() == MediaType.UNDEFINED)) {
            throw new T9tException(T9tIOException.OUTPUT_COMM_CHANNEL_REQUIRED);
        }

        try (IOutputSession os = Jdp.getRequired(IOutputSession.class)) {
            // open the session (creates the file, if using files)
            sinkRef = os.open(params.getParameters());
            OutputStream oStream = os.getOutputStream();
            oStream.write(params.getData().getBytes());
        } catch (Exception e) {
            // In case of any error returnCode <> 0.
            LOGGER.error("Exception occured during file storage (OutputSession) - Error Message {} ", e);
            throw new T9tException(T9tException.GENERAL_EXCEPTION, e.getMessage());
        }
        SinkCreatedResponse response = new SinkCreatedResponse();
        response.setReturnCode(0);
        response.setSinkRef(sinkRef);
        return response;
    }
}
