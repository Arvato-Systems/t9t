/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc.be.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.doc.T9tDocExtException;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.recipients.RecipientArchive;
import com.arvatosystems.t9t.doc.services.DocArchiveResult;
import com.arvatosystems.t9t.doc.services.IDocArchiveDistributor;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.core.MapComposer;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DocArchiveDistributor implements IDocArchiveDistributor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocArchiveDistributor.class);

    @Override
    public DocArchiveResult transmit(final RecipientArchive rcpt, final Function<MediaXType, MediaData> data, final MediaXType primaryFormat,
            final String documentTemplateId, final DocumentSelector documentSelector) {
        final IOutputSession outputSession = Jdp.getRequired(IOutputSession.class);
        final Map<String, Object> additionalParams = new HashMap<>(8);
        final Map<String, Object> map = MapComposer.marshal(documentSelector, false, false);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            additionalParams.put(entry.getKey(), entry.getValue().toString());
        }
        final OutputSessionParameters sessionParams = new OutputSessionParameters();
        sessionParams.setDataSinkId(rcpt.getDataSinkId());
        sessionParams.setOriginatorRef(rcpt.getOriginatorRef());
        sessionParams.setConfigurationRef(rcpt.getConfigurationRef());
        sessionParams.setGenericRefs1(rcpt.getGenericRefs1());
        sessionParams.setGenericRefs2(rcpt.getGenericRefs2());
        sessionParams.setCommunicationFormatType(rcpt.getCommunicationFormat() == null ? MediaTypes.MEDIA_XTYPE_UNDEFINED : rcpt.getCommunicationFormat());
        sessionParams.setAdditionalParameters(additionalParams);
        sessionParams.getAdditionalParameters().put("documentTemplateId", documentTemplateId);
        sessionParams.getAdditionalParameters().putAll(rcpt.getOutputSessionParameters());

        // where to assign in output session? TODO
        OutputStream outputStream = null;
        try {
            // open
            final Long sinkRef = outputSession.open(sessionParams);

            // check if conversion required
            final MediaXType requestedType = outputSession.getCommunicationFormatType();
            MediaData documentForStream = data.apply(requestedType);
            outputStream = outputSession.getOutputStream();

            if (documentForStream.getRawData() != null) {
                documentForStream.getRawData().toOutputStream(outputStream);
            } else if (documentForStream.getText() != null) {
                outputStream.write(documentForStream.getText().getBytes("UTF-8"));
            }

            final String fileOrQueueName = outputSession.getFileOrQueueName();
            outputStream.close();
            outputStream = null;
            return new DocArchiveResult(sinkRef, fileOrQueueName);
        } catch (final IOException e) {
            LOGGER.error("Unable to open the output session or write to it, due to : {} ", e);
            throw new T9tException(T9tDocExtException.DOCUMENT_CREATION_ERROR);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
                if (outputSession != null) {
                    outputSession.close();
                }
            } catch (final Exception e) {
                LOGGER.error("Unable to close the resources, due to : {} ", e);
            }
        }
    }
}
