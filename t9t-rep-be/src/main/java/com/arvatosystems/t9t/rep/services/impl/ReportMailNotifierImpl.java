/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.rep.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.ICrossModuleRefResolver;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.MailToUsersRequest;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.SinkCrudRequest;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.T9tRepException;
import com.arvatosystems.t9t.rep.services.IReportMailNotifier;
import com.google.common.collect.Lists;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;

@Singleton
public class ReportMailNotifierImpl implements IReportMailNotifier {

    private final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);
    private final ICrossModuleRefResolver crossModuleRefResolver = Jdp.getRequired(ICrossModuleRefResolver.class);
    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMailNotifierImpl.class);

    @Override
    public void sendEmail(final ReportConfigDTO reportConfigDTO, final ReportParamsDTO reportParamsDTO, final String mailingGroupId, final String docConfigId,
      final Long sinkRef, final DocumentSelector selector) {

        final RequestContext ctx = Jdp.getRequired(RequestContext.class);

        if (mailingGroupId != null && sinkRef != null) {

            final SinkDTO sinkDTO = crossModuleRefResolver.getData(new SinkCrudRequest(), sinkRef);
            final String absolutePath = fileUtil.getAbsolutePathForTenant(ctx.tenantId, sinkDTO.getFileOrQueueName());

            final Map<String, Object> zField = new HashMap<>();
            zField.put("attachmentName", sinkDTO.getFileOrQueueName());
            final MediaData mediaData = new MediaData();
            mediaData.setMediaType(sinkDTO.getCommFormatType());
            mediaData.setZ(zField);

            final File reportFile = new File(absolutePath);
            try (FileInputStream fis = new FileInputStream(reportFile)) {
                final int fileLength = (int) reportFile.length();
                final byte[] byteArray = new byte[fileLength];
                fis.read(byteArray, 0, fileLength);
                mediaData.setRawData(new ByteArray(byteArray));
            } catch (final IOException e) {
                LOGGER.error("Unable to send report email due to", e);
                throw new T9tException(T9tRepException.UNABLE_TO_NOTIFY_REPORT_COMPLETION);
            }

            final MailToUsersRequest request = new MailToUsersRequest();
            request.setMailingGroupId(mailingGroupId);
            request.setDocumentId(docConfigId);
            request.setDocumentSelector(selector);
            request.setAttachments(Lists.newArrayList(mediaData));

            final Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("r", reportConfigDTO);
            parameterMap.put("param", reportParamsDTO);

            request.setData(parameterMap);
            autonomousExecutor.execute(ctx, request);
        }
    }
}
