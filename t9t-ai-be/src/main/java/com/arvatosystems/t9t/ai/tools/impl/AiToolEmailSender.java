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
package com.arvatosystems.t9t.ai.tools.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.AiToolNoResult;
import com.arvatosystems.t9t.ai.tools.AiToolSendEmail;
import com.arvatosystems.t9t.authc.api.GetUserDataByUserIdRequest;
import com.arvatosystems.t9t.authc.api.GetUserDataResponse;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named(AiToolSendEmail.my$PQON)
@Singleton
public class AiToolEmailSender implements IAiTool<AiToolSendEmail, AiToolNoResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiToolEmailSender.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public AiToolNoResult performToolCall(final RequestContext ctx, final AiToolSendEmail request) {
        LOGGER.debug("Email tool called with subject {}", T9tUtil.nvl(request.getSubject(), "(none)"));

        final UserData userData = getUserData(ctx);

        final RecipientEmail recipient = new RecipientEmail();
        recipient.setCommunicationFormat(MediaXType.of(MediaType.TEXT));
        recipient.setTo(List.of(userData.getEmailAddress()));

        final EmailMessage msg = new EmailMessage();
        msg.setRecipient(recipient);
        msg.setMailSubject(T9tUtil.nvl(request.getSubject(), "The data you requested"));
        msg.setMailBody(wrapText(T9tUtil.nvl(request.getEmailText(),
          "Hello " + userData.getName() + ",\nHere is the data you requested.\n\nBest regards,\n    Your AI assistant")));
        msg.setAttachments(List.of(request.getAttachment()));

        final AiToolNoResult result = new AiToolNoResult();
        result.setMessage("Success");
        return result;
    }

    protected MediaData wrapText(final String text) {
        final MediaData md = new MediaData();
        md.setMediaType(MediaType.TEXT);
        md.setText(text);
        return md;
    }

    protected UserData getUserData(final RequestContext ctx) {
        final GetUserDataByUserIdRequest rq = new GetUserDataByUserIdRequest();
        rq.setUserId(ctx.userId);
        final GetUserDataResponse resp = executor.executeSynchronousAndCheckResult(ctx, rq, GetUserDataResponse.class);
        return resp.getUserData();
    }
}
