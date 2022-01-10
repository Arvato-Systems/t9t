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
package com.arvatosystems.t9t.email.be.aws.impl;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.services.IEmailSender;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IEmailSender using the AWS library.
 * Attachments are currently not supported by this implementation.
 */
@Singleton
@Named("SES")
public class AwsSesEmailService implements IEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsSesEmailService.class);

    protected static final String STANDARD_ENCODING     = "UTF-8";
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de";  // "smtp.gmail.com"

    @Override
    public ServiceResponse sendEmail(final Long messageRef, final UUID messageId, final EmailMessage msg, final EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending email ref {}, ID {} to {} using server {} via vert.x implementation", messageRef, messageId, msg.getRecipient().getTo().get(0),
                configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress());

        final RecipientEmail recipient = msg.getRecipient();
        final Destination destination = new Destination();

        if (recipient.getTo() != null) {
            destination.setToAddresses(recipient.getTo());
        }
        if (recipient.getCc() != null) {
            destination.setCcAddresses(recipient.getCc());
        }
        if (recipient.getBcc() != null) {
            destination.setBccAddresses(recipient.getBcc());
        }

        final SendEmailRequest sendRequest = new SendEmailRequest();
        sendRequest.setDestination(destination);
        if (recipient.getFrom() != null) {
            sendRequest.setSource(recipient.getFrom());
        }
        if (recipient.getReplyTo() != null) {
            sendRequest.setReplyToAddresses(Collections.singletonList(recipient.getReplyTo()));
        }

        final Message message = new Message();
        if (msg.getMailSubject() != null) {
            message.setSubject(asContent(msg.getMailSubject()));
        }

        final Body body = new Body();
        message.setBody(body);
        // set mail body
        if (MediaTypes.MEDIA_XTYPE_TEXT == msg.getMailBody().getMediaType()) {
            body.setText(asContent(msg.getMailBody().getText()));
        }
        if (MediaTypes.MEDIA_XTYPE_HTML == msg.getMailBody().getMediaType() || MediaTypes.MEDIA_XTYPE_XHTML == msg.getMailBody().getMediaType()) {
            body.setHtml(asContent(msg.getMailBody().getText()));
            if (msg.getAlternateBody() != null && MediaTypes.MEDIA_XTYPE_TEXT == msg.getAlternateBody().getMediaType()
                    && msg.getAlternateBody().getText() != null) {
                body.setText(asContent(msg.getAlternateBody().getText()));
            }
        }

        sendRequest.setMessage(message); // Set message to request

        final AmazonSimpleEmailServiceClient client = new AmazonSimpleEmailServiceClient();
        client.sendEmail(sendRequest);
        return new ServiceResponse();
    }

    protected Content asContent(final String input) {
        return new Content().withCharset("UTF-8").withData(input);
    }
}
