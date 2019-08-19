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
package com.arvatosystems.t9t.rep.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.request.UserSearchRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.ICrossModuleRefResolver;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.SinkCrudRequest;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.T9tRepException;
import com.arvatosystems.t9t.rep.services.IReportMailNotifier;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;

@Singleton
public class ReportMailNotifierImpl implements IReportMailNotifier {

    public static final String COMMA_SEPARATOR = ",";
    private final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);
    private final ICrossModuleRefResolver crossModuleRefResolver = Jdp.getRequired(ICrossModuleRefResolver.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportMailNotifierImpl.class);

    @Override
    public void sendEmail(ReportConfigDTO reportConfigDTO, ReportParamsDTO reportParamsDTO, String mailGroup, String docConfigId, Long sinkRef, DocumentSelector selector) {

        RequestContext ctx = Jdp.getRequired(RequestContext.class);

        RecipientEmail recipientEmail = getEmailByUserId(mailGroup);

        if (recipientEmail != null && sinkRef != null) {

            SinkDTO sinkDTO = crossModuleRefResolver.getData(new SinkCrudRequest(), sinkRef);
            String absolutePath = fileUtil.getAbsolutePathForTenant(ctx.tenantId, sinkDTO.getFileOrQueueName());

            Map<String, Object> zField = new HashMap<>();
            zField.put("attachmentName", sinkDTO.getFileOrQueueName());
            MediaData mediaData = new MediaData();
            mediaData.setMediaType(sinkDTO.getCommFormatType());
            mediaData.setZ(zField);

            File reportFile = new File(absolutePath);
            try (FileInputStream fis = new FileInputStream(reportFile)) {
                int fileLength = (int) reportFile.length();
                byte[] byteArray = new byte[fileLength];
                fis.read(byteArray, 0, fileLength);
                mediaData.setRawData(new ByteArray(byteArray));
            } catch (IOException e) {
                LOGGER.error("Unable to send report email due to", e);
                throw new T9tException(T9tRepException.UNABLE_TO_NOTIFY_REPORT_COMPLETION);
            }

            NewDocumentRequest request = new NewDocumentRequest();
            request.setDocumentId(docConfigId);
            request.setDocumentSelector(selector);
            request.setRecipientList(Lists.newArrayList(recipientEmail));
            request.setAttachments(Lists.newArrayList(mediaData));

            Map<String, Object> parameterMap = new HashMap<>();
            parameterMap.put("r", reportConfigDTO);
            parameterMap.put("param", reportParamsDTO);

            request.setData(parameterMap);
            autonomousExecutor.execute(ctx, request);
        } else {
            LOGGER.warn("Sink ref is {}, recipient email is {}. Not sending report email to users", sinkRef, recipientEmail);
        }
    }

    private RecipientEmail getEmailByUserId(String userSeparatedByComma) {

        Set<String> recipientEmails = new HashSet<>();

        if (!Strings.isNullOrEmpty(userSeparatedByComma)) {
            String[] userIds = userSeparatedByComma.split(COMMA_SEPARATOR);
            UserSearchRequest userSearchRequest = new UserSearchRequest();
            userSearchRequest.setSearchFilter(new UnicodeFilter("userId", null, null, null, null, Lists.newArrayList(userIds)));

            @SuppressWarnings("unchecked")
            ReadAllResponse<UserDTO, TrackingBase> userSearchResponse = executor.executeSynchronousAndCheckResult(userSearchRequest, ReadAllResponse.class);

            if (userSearchResponse != null && !userSearchResponse.getDataList().isEmpty()) {
                for (DataWithTracking<UserDTO, TrackingBase> dwt : userSearchResponse.getDataList()) {
                    if (dwt.getData().getEmailAddress() != null) {
                        recipientEmails.add(dwt.getData().getEmailAddress());
                    }
                }
            }
        }

        if (!recipientEmails.isEmpty()) {
            return new RecipientEmail(Lists.newArrayList(recipientEmails));
        } else {
            return null;
        }
    }

}
