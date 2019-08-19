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
package com.arvatosystems.t9t.email.be.smtp.impl

import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.api.EmailMessage
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.services.IEmailSender
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.Part
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.Multipart
import com.arvatosystems.t9t.base.T9tException
import javax.mail.MessagingException
import java.util.UUID
import javax.mail.BodyPart
import com.arvatosystems.t9t.email.T9tEmailException

// see https://cloud.google.com/appengine/docs/java/mail/usingjavamail
// gmail:
//Outgoing Mail (SMTP) Server
//requires TLS or SSL: smtp.gmail.com (use authentication)
//Use Authentication: Yes
//Port for TLS/STARTTLS: 587
//Port for SSL: 465


@AddLogger
@Singleton
@Named("SMTP")
class SmtpEmailService implements IEmailSender {
    protected static final String STANDARD_ENCODING     = "UTF-8"
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de"  // "smtp.gmail.com")

    def protected createSession(EmailModuleCfgDTO configuration) {
        val port = configuration.smtpServerPort ?: Integer.valueOf(if (configuration.smtpServerTls) 587 else 25)
        val props = new Properties() => [
            if (Boolean.TRUE == configuration.smtpServerTls)
                put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER)
            put("mail.smtp.port", port.toString)
            put("mail.mime.charset", STANDARD_ENCODING)
        ]
        if (configuration.smtpServerPassword !== null && configuration.smtpServerUserId !== null) {
            // authenticated access
            props.put("mail.smtp.auth", "true")
            return Session.getInstance(props, new Authenticator() {
                override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(configuration.smtpServerUserId, configuration.smtpServerPassword);
                }
            });
        } else {
            // unauthenticated access
            return Session.getInstance(props);
        }
    }

    def private MimeMessage composeMessage(Session session, EmailMessage msg) throws Exception {
        try {
            val RecipientEmail recipient = msg.recipient
            val emailMessage = new MimeMessage(session) => [
                if (msg.mailSubject   !== null) subject     = msg.mailSubject
                if (recipient.from    !== null) from        = new InternetAddress(recipient.from)
                if (recipient.replyTo !== null) replyTo     = #[ new InternetAddress(recipient.replyTo) ]
                if (recipient.to      !== null) recipient.to.forEach  [ address | addRecipient(Message.RecipientType.TO,  new InternetAddress(address)) ]
                if (recipient.cc      !== null) recipient.cc.forEach  [ address | addRecipient(Message.RecipientType.CC,  new InternetAddress(address)) ]
                if (recipient.bcc     !== null) recipient.bcc.forEach [ address | addRecipient(Message.RecipientType.BCC, new InternetAddress(address)) ]
            ]
            if (msg.attachments.nullOrEmpty && msg.mailBody.mediaType == MediaTypes.MEDIA_XTYPE_TEXT) {
                // simple email - special case
                emailMessage.setText(msg.mailBody.text, STANDARD_ENCODING)
            } else {
                emailMessage.setContent(buildMixed(msg))
            }
            return emailMessage
        } catch (MessagingException e) {
            LOGGER.error("SMTP message composition problem: {}: {}", e.class.simpleName, e.message)
            throw new T9tException(T9tEmailException.MIME_MESSAGE_COMPOSITION_PROBLEM, e.class.simpleName + ": " + e.message)
        }
    }

    def protected BodyPart toBodyPart(Multipart part) {
        val child     = new MimeBodyPart
        child.content = part
        return child
    }

    // create a mixed multipart from email body and attachments, if exist
    def Multipart buildMixed(EmailMessage msg) {
        if (msg.attachments.nullOrEmpty) {
            // no attachments - skip this layer
            return buildAlternative(msg)
        }
        // build a multipart of main message plus attachments
        val multipart = new MimeMultipart("mixed")
        multipart.addBodyPart(buildAlternative(msg).toBodyPart)
        msg.attachments.forEach[ thisAttachment |
            val mds = new MediaDataSource(thisAttachment)
            multipart.addBodyPart(new MimeBodyPart => [
                dataHandler = new DataHandler(mds)
                val attachmentName = thisAttachment.z?.get("attachmentName")?.toString
                if (attachmentName !== null) {
                    fileName = attachmentName
                    disposition = Part.ATTACHMENT
                }
            ])
        ]
        return multipart
    }

    // create a multipart with HTML and plain text alternatives
    def Multipart buildAlternative(EmailMessage msg) {
        if (msg.alternateBody === null) {
            // no text fallback required - skip this part
            return buildRelated(msg)
        }
        val alternateMimeType = MediaTypeInfo.getFormatByType(msg.alternateBody.mediaType)
        val multipart = new MimeMultipart("alternative")
        // text content first - iPad only renders last alternative
        multipart.addBodyPart(new MimeBodyPart => [
            setContent(msg.alternateBody.text, alternateMimeType.mimeType + ";charset=UTF-8")
        ])
        multipart.addBodyPart(buildRelated(msg).toBodyPart)
        return multipart
    }

    // build an HTML email with embedded images (CIDs)
    def Multipart buildRelated(EmailMessage msg) {
        val baseMimeType = MediaTypeInfo.getFormatByType(msg.mailBody.mediaType)
        val emailBody = new MimeBodyPart => [
            setContent(msg.mailBody.text, baseMimeType.mimeType + ";charset=UTF-8")
        ]
        // HTML always creates a multipart - but maybe using a single BodyPart only
//        if (msg.cids === null || msg.cids.isEmpty) {
//            // no embedded images - done
//            return emailBody
//        }
        // create a multipart email
        val multipart = new MimeMultipart("related")
        // content
        multipart.addBodyPart(emailBody)
        msg.cids.forEach[
            val mds = new MediaDataSource($1)
            multipart.addBodyPart(new MimeBodyPart => [
                dataHandler = new DataHandler(mds)
                contentID   = "<" + mds.name + ">"
                disposition = MimeBodyPart.INLINE
            ])
        ]
        return multipart
    }

    override sendEmail(Long messageRef, UUID messageId, EmailMessage msg, EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending SMTP email ref {}, ID {} to {} using server {}",
            messageRef, messageId, msg.recipient.to.get(0), configuration.smtpServerAddress ?: DEFAULT_SMTP_SERVER
        )
        val session     = createSession(configuration)
        val message     = composeMessage(session, msg)
        val transport   = session.getTransport(configuration.smtpServerTransport)
        try {
            if (configuration.smtpServerPassword === null)
                transport.connect
            else
                transport.connect(configuration.smtpServerUserId, configuration.smtpServerPassword) // isn't this double? has been specified above
            LOGGER.debug("Connected to SMTP server")
            transport.sendMessage(message, message.allRecipients)
            LOGGER.debug("Email message sent")
        } catch (Exception e) {
            LOGGER.error("Connection or message sending error: {}: {}", e.class.simpleName, e.message)
            if (transport !== null)
                transport.close
            throw new T9tException(T9tEmailException.SMTP_ERROR, e.message)
        }
        transport.close
        LOGGER.debug("Disconnected from SMTP server")
        return null
    }
}
