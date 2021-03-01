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
package com.arvatosystems.t9t.doc.be.api

import com.arvatosystems.t9t.authc.api.GetMultipleUserDataResponse
import com.arvatosystems.t9t.authc.api.GetUserDataByUserIdsRequest
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.MailingGroupKey
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.MailToUsersRequest
import com.arvatosystems.t9t.doc.api.NewDocumentRequest
import com.arvatosystems.t9t.doc.jpa.persistence.IMailingGroupEntityResolver
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.google.common.base.Strings
import com.google.common.collect.Lists
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import java.util.ArrayList
import java.util.Collections

@AddLogger
class MailToUsersRequestHandler extends AbstractRequestHandler<MailToUsersRequest> {
    @Inject IExecutor executor
    @Inject IMailingGroupEntityResolver resolver

    override execute(RequestContext ctx, MailToUsersRequest rq) throws Exception {
        // resolve the mailingGroup
        val mailingGroup = resolver.getEntityData(new MailingGroupKey(rq.mailingGroupId), false)
        val emailAddresses = getEmailByUserId(ctx, mailingGroup.mailingList)
        if (emailAddresses.isEmpty) {
            LOGGER.info("No email sent to group {} - no users with email addresses found", rq.mailingGroupId)
            return ok
        }
        val request = new NewDocumentRequest();
        request.setDocumentId(rq.documentId ?: mailingGroup.docConfigId);
        request.setDocumentSelector(rq.documentSelector ?: new DocumentSelector("-", ctx.internalHeaderParameters.getLanguageCode() ?: "en", "XX", "XXX"));
        request.setRecipientList(Collections.singletonList(new RecipientEmail(emailAddresses)))
        request.setAttachments(rq.attachments);
        request.setData(rq.data);
        request.timeZone = rq.timeZone
        executor.executeAsynchronous(ctx, request)
        return ok
    }

    def getEmailByUserId(RequestContext ctx, String usersSeparatedByComma) {
        if (!Strings.isNullOrEmpty(usersSeparatedByComma)) {
            val userIds = usersSeparatedByComma.split(",");
            val userSearchRequest = new GetUserDataByUserIdsRequest();
            userSearchRequest.userIds = Lists.newArrayList(userIds)
            val userSearchResponse = executor.executeSynchronousAndCheckResult(ctx, userSearchRequest, GetMultipleUserDataResponse);
            val emailAddresses = new ArrayList(userSearchResponse.userData.size)
            userSearchResponse.userData.filterNull.forEach[emailAddresses.add(emailAddress)]
            return emailAddresses
        }
        return Collections.emptyList;
    }
}
