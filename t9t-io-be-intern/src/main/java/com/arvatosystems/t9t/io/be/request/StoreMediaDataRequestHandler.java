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
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.request.StoreMediaDataRequest;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;

/**
 * An example implementation of a file export service request. For simplicity, we inherit from an existing method which provides the required data. Rev. 2,
 * using try with resources.
 */
public class StoreMediaDataRequestHandler extends AbstractRequestHandler<StoreMediaDataRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreMediaDataRequestHandler.class);

    @Override
    public ServiceResponse execute(StoreMediaDataRequest params) throws Exception {
        Long sinkRef;
        final MediaData m = params.getMediaData();

        // create outputSessionParameters
        OutputSessionParameters osp = new OutputSessionParameters();
        osp.setDataSinkId(params.getDataSinkId());
        osp.setCommunicationFormatType(m.getMediaType());
        osp.setOriginatorRef   (params.getOriginatorRef());
        osp.setConfigurationRef(params.getConfigurationRef());
        osp.setGenericRefs1    (params.getGenericRefs1());
        osp.setGenericRefs2    (params.getGenericRefs2());

        try (IOutputSession os = Jdp.getRequired(IOutputSession.class)) {
            // open the session (creates the file, if using files)
            sinkRef = os.open(osp);
            OutputStream oStream = os.getOutputStream();
            if (m.getRawData() != null) {
                m.getRawData().toOutputStream(oStream);
            } else if (m.getText() != null) {
                oStream.write(m.getText().getBytes(StandardCharsets.UTF_8));
            }
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
