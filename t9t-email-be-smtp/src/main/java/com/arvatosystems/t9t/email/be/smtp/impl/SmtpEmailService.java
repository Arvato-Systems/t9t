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
package com.arvatosystems.t9t.email.be.smtp.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.T9tEmailException;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.services.IEmailSender;
import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import java.util.Properties;
import java.util.UUID;
import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("SMTP")
public class SmtpEmailService implements IEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpEmailService.class);

    protected static final String STANDARD_ENCODING     = "UTF-8";
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de";  // "smtp.gmail.com")

    @Override
    public ServiceResponse sendEmail(final Long messageRef, final UUID messageId, final EmailMessage msg, final EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending SMTP email ref {}, ID {} to {} using server {}",
            messageRef, messageId, msg.getRecipient().getTo().get(0),
            configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress()
        );
        try {
            final Session session = createSession(configuration);
            final MimeMessage message = composeMessage(session, msg);
            final Transport transport = session.getTransport(configuration.getSmtpServerTransport());
            try {
                if (configuration.getSmtpServerPassword() == null)
                    transport.connect();
                else
                    transport.connect(configuration.getSmtpServerUserId(), configuration.getSmtpServerPassword()); // isn't this double? has been specified
                                                                                                                   // above
                LOGGER.debug("Connected to SMTP server");
                transport.sendMessage(message, message.getAllRecipients());
                LOGGER.debug("Email message sent");
            } catch (Exception e) {
                LOGGER.error("Connection or message sending error: {}: {}", e.getClass().getSimpleName(), e.getMessage());
                if (transport != null)
                    transport.close();

                throw new T9tException(T9tEmailException.SMTP_ERROR, e.getMessage());
            }
            transport.close();
        } catch (Exception e1) {
            e1.printStackTrace();
            LOGGER.error("Error while sending email: {}", e1.getMessage());
        }
        LOGGER.debug("Disconnected from SMTP server");
        return null;
    }

    protected Session createSession(EmailModuleCfgDTO configuration) {
        final Integer port = configuration.getSmtpServerPort() == null ? (configuration.getSmtpServerTls() ? 587 : 25) : configuration.getSmtpServerPort();
        final Properties props = new Properties();
        if (Boolean.TRUE.equals(configuration.getSmtpServerTls())) {
            props.put("mail.smtp.starttls.enable", "true");
        }
        props.put("mail.smtp.host", configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress());
        props.put("mail.smtp.port", port.toString());
        props.put("mail.mime.charset", STANDARD_ENCODING);
        if (configuration.getSmtpServerPassword() != null && configuration.getSmtpServerUserId() != null) {
            // authenticated access
            props.put("mail.smtp.auth", "true");
            return Session.getInstance(props, new Authenticator() {
                @Override protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(configuration.getSmtpServerUserId(), configuration.getSmtpServerPassword());
                }
            });
        } else {
            // unauthenticated access
            return Session.getInstance(props);
        }
    }

    private MimeMessage composeMessage(Session session, EmailMessage msg) throws Exception {
        try {
            final RecipientEmail recipient = msg.getRecipient();
            final MimeMessage emailMessage = new MimeMessage(session);
            if (msg.getMailSubject()   != null) {
                emailMessage.setSubject(msg.getMailSubject());
            }

            if (recipient.getFrom() != null) {
                emailMessage.setFrom(new InternetAddress(recipient.getFrom()));
            }

            if (recipient.getReplyTo() != null) {
                emailMessage.setReplyTo(new Address[] {new InternetAddress(recipient.getReplyTo())});
            }

            if (recipient.getTo() != null) {
                for (String address : recipient.getTo()) {
                    emailMessage.addRecipient(Message.RecipientType.TO,  new InternetAddress(address));
                }
            }

            if (recipient.getCc() != null) {
                for (String address : recipient.getCc()) {
                    emailMessage.addRecipient(Message.RecipientType.CC,  new InternetAddress(address));
                }
            }

            if (recipient.getBcc() != null) {
                for (String address : recipient.getBcc()) {
                    emailMessage.addRecipient(Message.RecipientType.BCC,  new InternetAddress(address));
                }
            }

            if ((msg.getAttachments() == null || msg.getAttachments().isEmpty()) && MediaTypes.MEDIA_XTYPE_TEXT.equals(msg.getMailBody().getMediaType())) {
                // simple email - special case
                emailMessage.setText(msg.getMailBody().getText(), STANDARD_ENCODING);
            } else {
                emailMessage.setContent(buildMixed(msg));
            }
            return emailMessage;
        } catch (MessagingException e) {
            LOGGER.error("SMTP message composition problem: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new T9tException(T9tEmailException.MIME_MESSAGE_COMPOSITION_PROBLEM, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private Multipart buildMixed(final EmailMessage msg) throws MessagingException {
        if (msg.getAttachments() == null || msg.getAttachments().isEmpty()) {
            // no attachments - skip this layer
            return buildAlternative(msg);
        }
        // build a multipart of main message plus attachments
        final MimeMultipart multipart = new MimeMultipart("mixed");
        multipart.addBodyPart(toBodyPart(buildAlternative(msg)));
        for (MediaData thisAttachment : msg.getAttachments()) {
            MediaDataSource mds = new MediaDataSource(thisAttachment);
            final MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setDataHandler(new DataHandler(mds));
            if (thisAttachment.getZ() != null && thisAttachment.getZ().get("attachmentName") != null) {
                bodyPart.setFileName(thisAttachment.getZ().get("attachmentName").toString());
                bodyPart.setDisposition(Part.ATTACHMENT);
            }
            multipart.addBodyPart(bodyPart);
        }

        return multipart;
    }

    private Multipart buildAlternative(EmailMessage msg) throws MessagingException {
        if (msg.getAlternateBody() == null) {
            // no text fallback required - skip this part
            return buildRelated(msg);
        }
        final MediaTypeDescriptor alternateMimeType = MediaTypeInfo.getFormatByType(msg.getAlternateBody().getMediaType());
        final MimeMultipart multipart = new MimeMultipart("alternative");
        // text content first - iPad only renders last alternative
        final MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent(msg.getAlternateBody().getText(), alternateMimeType.getMimeType() + ";charset=UTF-8");
        multipart.addBodyPart(toBodyPart(buildRelated(msg)));
        return multipart;
    }

    protected BodyPart toBodyPart(Multipart part) throws MessagingException {
        final MimeBodyPart child = new MimeBodyPart();
        child.setContent(part);
        return child;
    }

    private Multipart buildRelated(EmailMessage msg) throws MessagingException {
        final MediaTypeDescriptor baseMimeType = MediaTypeInfo.getFormatByType(msg.getMailBody().getMediaType());
        final MimeBodyPart emailBody = new MimeBodyPart();
        emailBody.setContent(msg.getMailBody().getText(), baseMimeType.getMimeType() + ";charset=UTF-8");
        // HTML always creates a multipart - but maybe using a single BodyPart only
//        if (msg.cids === null || msg.cids.isEmpty) {
//            // no embedded images - done
//            return emailBody
//        }
        // create a multipart email
        final MimeMultipart multipart = new MimeMultipart("related");
        // content
        multipart.addBodyPart(emailBody);
        for (MediaData cid : msg.getCids().values()) {
            final MediaDataSource mds = new MediaDataSource(cid);
            final MimeBodyPart part2 = new MimeBodyPart();
            part2.setDataHandler(new DataHandler(mds));
            part2.setContentID("<" + mds.getName() + ">");
            part2.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(part2);
        }
        return multipart;
    }
}
