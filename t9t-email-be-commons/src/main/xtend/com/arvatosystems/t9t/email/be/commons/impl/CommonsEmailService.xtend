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
package com.arvatosystems.t9t.email.be.commons.impl

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.T9tEmailException
import com.arvatosystems.t9t.email.api.EmailMessage
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.be.smtp.impl.MediaDataSource
import com.arvatosystems.t9t.email.services.IEmailSender
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import java.util.List
import java.util.Map
import java.util.UUID
import javax.mail.MessagingException
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.Email
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.MultiPartEmail
import org.apache.commons.mail.SimpleEmail

/** Implementation of IEmailSender using the Apache Commons-Email library (which is a wrapper around javax.mail). */
@AddLogger
@Singleton
@Named("COMMONS")
class CommonsEmailService implements IEmailSender {
    protected static final String STANDARD_ENCODING     = "UTF-8"
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de"  // "smtp.gmail.com")

    def protected void createSession(Email email, EmailModuleCfgDTO configuration) {
        val port = configuration.smtpServerPort ?: Integer.valueOf(if (configuration.smtpServerTls) 587 else 25)
        if (Boolean.TRUE == configuration.smtpServerTls)
            email.SSLOnConnect = true
        email.hostName = configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER
        email.smtpPort = port
        // put("mail.mime.charset", STANDARD_ENCODING)

        if (configuration.smtpServerPassword !== null && configuration.smtpServerUserId !== null) {
            // authenticated access
            email.authenticator = new DefaultAuthenticator(configuration.smtpServerUserId, configuration.smtpServerPassword)
        }
    }

    def private Email composeMessage(RecipientEmail recipient, EmailMessage msg) throws Exception {
        try {
            if (msg.attachments.nullOrEmpty && msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_TEXT) {
                // simple email - shortcut for special case
                val email = new SimpleEmail
                email.charset = STANDARD_ENCODING
                email.msg = msg.mailBody.text
                return email
            } else {
                val email = new HtmlEmail
                email.charset = STANDARD_ENCODING
                email.htmlMsg = msg.mailBody.text
                if (msg.alternateBody !== null)
                    email.textMsg = msg.alternateBody.text
                if (msg.attachments !== null)
                    email.addAttachments(msg.attachments)
                if (msg.cids !== null)
                    email.addCids(msg.cids)
                return email
            }
        } catch (MessagingException e) {
            LOGGER.error("SMTP message composition problem: {}: {}", e.class.simpleName, e.message)
            throw new T9tException(T9tEmailException.MIME_MESSAGE_COMPOSITION_PROBLEM, e.class.simpleName + ": " + e.message)
        }
    }

    // add attachments to a multipart email
    def void addAttachments(MultiPartEmail email, List<MediaData> attachments) {
        attachments.forEach[
            val mds = new MediaDataSource(it)
            val fileName = z?.get("attachmentName")?.toString
            email.attach(mds, fileName, fileName)
        ]
    }

    // add CIDs to an HTML email
    def void addCids(HtmlEmail email, Map<String, MediaData> cids) {
        cids.forEach[
            val mds = new MediaDataSource($1)
            email.embed(mds, mds.name, mds.name)
        ]
    }

    override sendEmail(Long messageRef, UUID messageId, EmailMessage msg, EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending email ref {}, ID {} to {} using server {} via COMMONS library",
            messageRef, messageId, msg.recipient.to.get(0), configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER
        )

        val RecipientEmail recipient = msg.recipient
        val email = composeMessage(recipient, msg)
        email.createSession(configuration)

        if (msg.mailSubject   !== null) email.subject     = msg.mailSubject
        if (recipient.from    !== null) email.from        = recipient.from
        if (recipient.replyTo !== null) email.addReplyTo(recipient.replyTo)
        if (recipient.to      !== null) recipient.to.forEach  [ email.addTo(it) ]
        if (recipient.cc      !== null) recipient.cc.forEach  [ email.addCc(it) ]
        if (recipient.bcc     !== null) recipient.bcc.forEach [ email.addBcc(it) ]

        email.send
        return null
    }
}
