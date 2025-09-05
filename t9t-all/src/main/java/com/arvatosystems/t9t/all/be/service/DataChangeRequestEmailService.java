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
package com.arvatosystems.t9t.all.be.service;

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import com.arvatosystems.t9t.changeRequest.services.IDataChangeRequestEmailService;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.google.common.collect.ImmutableMap;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.OperationTypes;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Singleton
public class DataChangeRequestEmailService implements IDataChangeRequestEmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataChangeRequestEmailService.class);

    protected final IAuthPersistenceAccess authPersistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);
    protected final IChangeWorkFlowConfigCache configCache = Jdp.getRequired(IChangeWorkFlowConfigCache.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public void sendReviewEmail(@Nonnull final RequestContext ctx, @Nonnull final DataChangeRequestDTO data) {
        LOGGER.debug("Sending email to review data change request {} having status {}", data.getChangeId(), data.getStatus());
        try {
            final ChangeWorkFlowConfigDTO config = configCache.getOrNull(data.getPqon());
            final boolean separateActivation = config != null && config.getSeparateActivation();
            final List<UserData> userDataList = new ArrayList<>(authPersistenceAccess.getUsersWithPermission(ctx.internalHeaderParameters.getJwtInfo(),
                PermissionType.BACKEND, data.getCrudRequest().ret$PQON(), OperationTypes.ofTokens(OperationType.REJECT)));
            final OperationTypes operationTypes = separateActivation ?  OperationTypes.ofTokens(OperationType.APPROVE)
                : OperationTypes.ofTokens(OperationType.ACTIVATE, OperationType.APPROVE);
            userDataList.addAll(authPersistenceAccess.getUsersWithPermission(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND,
                    data.getCrudRequest().ret$PQON(), operationTypes));
            final List<String> emails = getEmails(ctx, userDataList);
            if (emails.isEmpty()) {
                LOGGER.warn("No email address found for users to review data change request {}", data.getChangeId());
            }
            final NewDocumentRequest docReq = new NewDocumentRequest();
            docReq.setDocumentId(T9tConstants.DOCUMENT_ID_APPROVAL_REQUEST);
            docReq.setDocumentSelector(DocConstants.GENERIC_DOCUMENT_SELECTOR);
            docReq.setData(ImmutableMap.of(T9tConstants.DOC_PREFIX_APPROVAL_REQUEST, data));
            docReq.setRecipientList(List.of(new RecipientEmail(emails)));
            docReq.setAttachments(Collections.emptyList());
            executor.executeSynchronous(docReq);
        } catch (final Exception ex) {
            LOGGER.error("Error while sending email to review data change request {}", data.getChangeId(), ex);
        }
    }

    @Nonnull
    private List<String> getEmails(@Nonnull final RequestContext ctx, @Nonnull final List<UserData> userDataList) {
        final List<String> emails = new ArrayList<>(userDataList.size());
        for (final UserData userData : userDataList) {
            if (!userData.getUserId().equals(ctx.userId) && T9tUtil.isNotBlank(userData.getEmailAddress())) {
                emails.add(userData.getEmailAddress());
            }
        }
        return emails;
    }
}
