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
package com.arvatosystems.t9t.email.be.aws.impl

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.api.EmailMessage
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.services.IEmailSender
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import java.util.UUID

/** Implementation of IEmailSender using the AWS library.
 * Attachments are currently not supported by this implementation.
 */
@AddLogger
@Singleton
@Named("SES")
class AwsSesEmailService implements IEmailSender {
    protected static final String STANDARD_ENCODING     = "UTF-8"
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de"  // "smtp.gmail.com")

//    // add attachments to a multipart email
//    def void addAttachments(MultiPartEmail email, List<MediaData> attachments) {
//        attachments.forEach[
//            val mds = new MediaDataSource(it)
//            val fileName = z?.get("attachmentName")?.toString
//            email.attach(mds, fileName, fileName)
//        ]
//    }
//
//    // add CIDs to an HTML email
//    def void addCids(HtmlEmail email, Map<String, MediaData> cids) {
//        cids.forEach[
//            val mds = new MediaDataSource($1)
//            email.embed(mds, mds.name, mds.name)
//        ]
//    }

    def protected asContent(String input) {
        return (new Content).withCharset("UTF-8").withData(input)
    }

    override sendEmail(Long messageRef, UUID messageId, EmailMessage msg, EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending email ref {}, ID {} to {} using server {} via vert.x implementation",
            messageRef, messageId, msg.recipient.to.get(0), configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER
        )
        val sendRequest = new SendEmailRequest
        val RecipientEmail recipient = msg.recipient
        val destination = new Destination
        if (recipient.to      !== null) destination.toAddresses          = recipient.to
        if (recipient.cc      !== null) destination.ccAddresses          = recipient.cc
        if (recipient.bcc     !== null) destination.bccAddresses         = recipient.bcc

        sendRequest.destination = destination
        if (recipient.from    !== null) sendRequest.source               = recipient.from
        if (recipient.replyTo !== null) sendRequest.replyToAddresses     = #[ recipient.replyTo ]

        val message = new Message
        if (msg.mailSubject   !== null) message.subject                  = msg.mailSubject.asContent

        val body = new Body

        message.body = body
        // set mail body
        if (msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_TEXT) {
            body.text        = msg.mailBody.text.asContent
        }
        if (msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_HTML || msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_XHTML) {
            body.html        = msg.mailBody.text.asContent
            if (MediaTypes.MEDIA_XTYPE_TEXT == msg.alternateBody?.mediaType && msg.alternateBody.text !== null)
                body.text    = msg.alternateBody.text.asContent
        }

        val client = new AmazonSimpleEmailServiceClient
        client.sendEmail(sendRequest)
        return null
    }
}
