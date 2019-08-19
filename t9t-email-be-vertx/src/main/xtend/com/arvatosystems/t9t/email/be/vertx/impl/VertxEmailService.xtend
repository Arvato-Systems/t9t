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
package com.arvatosystems.t9t.email.be.vertx.impl

import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.api.EmailMessage
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.services.IEmailSender
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Jdp
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import de.jpaw.util.ExceptionUtil
import io.vertx.core.Vertx
import io.vertx.ext.mail.MailAttachment
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailConfig
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.mail.StartTLSOptions
import java.util.ArrayList
import java.util.List
import java.util.Map
import java.util.UUID

/** Implementation of IEmailSender using the vert.x client. This is an asynchronous implementation. */
@AddLogger
@Singleton
@Named("VERTX")
class VertxEmailService implements IEmailSender {
    protected static final String STANDARD_ENCODING     = "UTF-8"
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de"  // "smtp.gmail.com")

    def protected MailClient createClient(EmailModuleCfgDTO configuration) {
        val clientPoolId = Integer.toString(configuration.hashCode)
        val config = new MailConfig => [
            hostname        = configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER
            port            = configuration.smtpServerPort ?: Integer.valueOf(if (configuration.smtpServerTls) 587 else 25)
            if (Boolean.TRUE == configuration.smtpServerTls)
                starttls    = StartTLSOptions.REQUIRED
            if (!configuration.smtpServerUserId.nullOrEmpty)
                username    = configuration.smtpServerUserId
            if (!configuration.smtpServerPassword.nullOrEmpty)
                password    = configuration.smtpServerPassword
            maxPoolSize     = 4 // default is 10 which may be a bit too much
        ]
        val vertx = Jdp.getRequired(Vertx)
        return MailClient.createShared(vertx, config, clientPoolId)
    }

    // add attachments to the message - the list of attachments is not empty
    def List<MailAttachment> toVertx(List<MediaData> attachments) {
        return attachments.map[
            val mds = new MediaDataSource(it)
            val fileName = z?.get("attachmentName")?.toString
            return new MailAttachment => [
                contentType = mds.contentType
                data        = mds.asBuffer
                if (fileName !== null)
                   name     = fileName
            ]
        ].toList
    }

    // add CIDs to an HTML email
    // add inline attachments to the message - the list of attachments is not empty
    def List<MailAttachment> toVertx(Map<String, MediaData> cids) {
        val result = new ArrayList<MailAttachment>(cids.size)
        cids.forEach[
            val mds = new MediaDataSource($1)
            result.add(new MailAttachment => [
                contentType = mds.contentType
                data        = mds.asBuffer
                contentId   = mds.name
                disposition = "inline"
            ])
        ]
        return result
    }

    override sendEmail(Long messageRef, UUID messageId, EmailMessage msg, EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending email ref {}, ID {} to {} using server {} via vert.x implementation",
            messageRef, messageId, msg.recipient.to.get(0), configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER
        )

        val RecipientEmail recipient = msg.recipient
        val message = new MailMessage
        if (recipient.replyTo !== null) message.addHeader("Reply-To", recipient.replyTo)  // no direct method provided
        if (msg.mailSubject   !== null) message.subject     = msg.mailSubject
        if (recipient.from    !== null) message.from        = recipient.from
        if (recipient.to      !== null) message.to          = recipient.to
        if (recipient.cc      !== null) message.cc          = recipient.cc
        if (recipient.bcc     !== null) message.bcc         = recipient.bcc

        // set mail body
        if (msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_TEXT) {
            message.text        = msg.mailBody.text
        }
        if (msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_HTML || msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_XHTML) {
            message.html        = msg.mailBody.text
            if (MediaTypes.MEDIA_XTYPE_TEXT == msg.alternateBody?.mediaType)
                message.text    = msg.alternateBody?.text
        }

        // set attachments
        if (msg.cids !== null && !msg.cids.empty) message.inlineAttachment = msg.cids.toVertx
        if (!msg.attachments.nullOrEmpty)         message.attachment       = msg.attachments.toVertx

        // create or reuse a shared client. To support changed parameters, the hash code of the configuration is used as identifier
        val mailClient = createClient(configuration)

        mailClient.sendMail(message, [
            if (succeeded) {
                LOGGER.debug("Email of ref {}, UUID {} has been sent, result = {}", messageRef, messageId, result)
            } else {
                LOGGER.error("Email of ref {}, UUID {} could not be sent, cause = {}", messageRef, messageId, ExceptionUtil.causeChain(cause))
            }
        ])
        return null
    }
}
