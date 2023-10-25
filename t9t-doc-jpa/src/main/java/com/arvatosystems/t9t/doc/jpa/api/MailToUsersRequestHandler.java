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
package com.arvatosystems.t9t.doc.jpa.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.authc.api.GetMultipleUserDataResponse;
import com.arvatosystems.t9t.authc.api.GetUserDataByUserIdsRequest;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.Recipient;
import com.arvatosystems.t9t.doc.MailingGroupKey;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.MailToUsersRequest;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.doc.jpa.entities.MailingGroupEntity;
import com.arvatosystems.t9t.doc.jpa.persistence.IMailingGroupEntityResolver;
import com.arvatosystems.t9t.email.api.RecipientEmail;

import de.jpaw.dp.Jdp;

public class MailToUsersRequestHandler extends AbstractRequestHandler<MailToUsersRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailToUsersRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final IMailingGroupEntityResolver resolver = Jdp.getRequired(IMailingGroupEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final MailToUsersRequest rq) throws Exception {
        // resolve the mailingGroup
        final MailingGroupEntity mailingGroup = this.resolver.getEntityData(new MailingGroupKey(rq.getMailingGroupId()), false);
        final List<String> emailAddresses = this.getEmailByUserId(ctx, mailingGroup.getMailingList());
        if (emailAddresses.isEmpty()) {
            LOGGER.info("No email sent to group {} - no users with email addresses found", rq.getMailingGroupId());
            return ok();
        }
        final NewDocumentRequest request = new NewDocumentRequest();
        request.setDocumentId(rq.getDocumentId() == null ? mailingGroup.getDocConfigId() : rq.getDocumentId());
        if (rq.getDocumentSelector() != null) {
            request.setDocumentSelector(rq.getDocumentSelector());
        } else {
            request.setDocumentSelector(new DocumentSelector("-",
                    ctx.internalHeaderParameters.getLanguageCode() == null ? "en" : ctx.internalHeaderParameters.getLanguageCode(), "XX", "XXX"));
        }
        request.setRecipientList(Collections.<Recipient>singletonList(new RecipientEmail(emailAddresses)));
        request.setAttachments(rq.getAttachments());
        request.setData(rq.getData());
        request.setTimeZone(rq.getTimeZone());
        executor.executeAsynchronous(ctx, request);
        return ok();
    }

    public List<String> getEmailByUserId(final RequestContext ctx, final String usersSeparatedByComma) {
        if (usersSeparatedByComma != null && !usersSeparatedByComma.isEmpty()) {
            final String[] userIds = usersSeparatedByComma.split(",");
            final GetUserDataByUserIdsRequest userSearchRequest = new GetUserDataByUserIdsRequest();
            final List<String> emailAddresses = new ArrayList<>(userIds.length);
            final List<String> userIdsToBeMapped = new ArrayList<>(userIds.length);
            splitEmailAddresses(emailAddresses, userIdsToBeMapped, userIds);
            userSearchRequest.setUserIds(userIdsToBeMapped);
            final GetMultipleUserDataResponse userSearchResponse = this.executor.executeSynchronousAndCheckResult(ctx, userSearchRequest,
                    GetMultipleUserDataResponse.class);
            for (final UserData userData : userSearchResponse.getUserData()) {
                if (userData != null) {
                    emailAddresses.add(userData.getEmailAddress());
                }
            }
            return emailAddresses;
        }

        return Collections.emptyList();
    }

    /**
     * Preprocess the IDs, split between userIds and email addresses.
     * An ID is considered to be an email address if it contains an at sign followed by a dot.
     */
    private void splitEmailAddresses(final List<String> emailAddresses, final List<String> userIdsToBeMapped, final String[] userIds) {
        for (final String idOrEmailAddress: userIds) {
            final int posOfAtSign = idOrEmailAddress.indexOf('@');
            if (posOfAtSign > 0 && idOrEmailAddress.indexOf('.', posOfAtSign) > 0) {
                emailAddresses.add(idOrEmailAddress);
            } else {
                userIdsToBeMapped.add(idOrEmailAddress);
            }
        }
    }
}
