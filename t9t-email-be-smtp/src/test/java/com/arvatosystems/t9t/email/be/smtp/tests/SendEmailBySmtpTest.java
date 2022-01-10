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
package com.arvatosystems.t9t.email.be.smtp.tests;

import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.be.smtp.impl.SmtpEmailService;
import com.google.common.collect.ImmutableList;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SendEmailBySmtpTest {
    private static final String DEFAULT_USER = "xxx@online.de";
    private static final String DEFAULT_PASSWORD = "yyy";
    private static final String SMTP = "SMTP";
    private static final String SMTP_ADDRESS = "smtp.1und1.de";
    private static final int SMTP_PORT = 587;

    private static final EmailModuleCfgDTO MODULE_CONFIG;
    private static final RecipientEmail RECIPIENT;

    static {
        MODULE_CONFIG = new EmailModuleCfgDTO();
        MODULE_CONFIG.setImplementation(SMTP);
        MODULE_CONFIG.setSmtpServerTransport(SMTP.toLowerCase());
        MODULE_CONFIG.setSmtpServerAddress(SMTP_ADDRESS);
        MODULE_CONFIG.setSmtpServerUserId(DEFAULT_USER);
        MODULE_CONFIG.setSmtpServerPassword(DEFAULT_PASSWORD);
        MODULE_CONFIG.setSmtpServerPort(SMTP_PORT);
        MODULE_CONFIG.setSmtpServerTls(Boolean.TRUE);

        RECIPIENT = new RecipientEmail();
        RECIPIENT.setTo(ImmutableList.of("jpaw@online.de"));
        RECIPIENT.setFrom(DEFAULT_USER);
        RECIPIENT.setCc(ImmutableList.of(DEFAULT_USER));
        RECIPIENT.setReplyTo("noOne@example.com");
    }

    @Disabled
    @Test
    public void testSimpleEmail() {
        final MediaData mediaData = new MediaData();
        mediaData.setMediaType(MediaTypes.MEDIA_XTYPE_TEXT);
        mediaData.setText("How are you today?\\n\\nBest regards...");
        final EmailMessage message = new EmailMessage(RECIPIENT, "Hello world", mediaData, null, null, null);
        final SmtpEmailService service = new SmtpEmailService();
        service.sendEmail(System.currentTimeMillis(), UUID.randomUUID(), message, MODULE_CONFIG);
    }

    @Disabled
            @Test
            public void testHtmlEmail() {

        final MediaData mediaData = new MediaData();
        final String html = "<html>\r\n"
                + "   <body>\r\n"
                + "     My <b>dear</b> friend John,\r\n"
                + "     <p>\r\n"
                + "     you should know that 28 &lt; 42.\r\n"
                + "   </body>\r\n"
                + " </html>";
        mediaData.setMediaType(MediaTypes.MEDIA_XTYPE_HTML);
        mediaData.setText(html);
        final EmailMessage message = new EmailMessage(RECIPIENT, "Hello John", mediaData, null, null, mediaData);
        final SmtpEmailService service = new SmtpEmailService();
        service.sendEmail(System.currentTimeMillis(), UUID.randomUUID(), message, MODULE_CONFIG);
    }
}
