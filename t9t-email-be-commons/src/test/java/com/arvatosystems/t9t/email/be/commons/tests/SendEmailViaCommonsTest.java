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
package com.arvatosystems.t9t.email.be.commons.tests;

import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.be.commons.impl.CommonsEmailService;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class SendEmailViaCommonsTest {
    private static final String DEFAULT_USER     = "xxx@online.de";
    private static final String DEFAULT_PASSWORD = "yyy";
    private static final EmailModuleCfgDTO MODULE_CONFIG;
    private static final RecipientEmail RECIPIENT;

    static {
        final EmailModuleCfgDTO emailModuleCfgDTO = new EmailModuleCfgDTO();
        emailModuleCfgDTO.setImplementation("COMMONS");
        emailModuleCfgDTO.setSmtpServerTransport("smtp");
        emailModuleCfgDTO.setSmtpServerAddress("smtp.1und1.de");
        emailModuleCfgDTO.setSmtpServerUserId(DEFAULT_USER);
        emailModuleCfgDTO.setSmtpServerPassword(DEFAULT_PASSWORD);
        emailModuleCfgDTO.setSmtpServerPort(587);
        emailModuleCfgDTO.setSmtpServerTls(Boolean.TRUE);
        MODULE_CONFIG = emailModuleCfgDTO;

        final RecipientEmail recipientEmail = new RecipientEmail();
        recipientEmail.setTo(Collections.singletonList("jpaw@online.de"));
        recipientEmail.setFrom(DEFAULT_USER);
        recipientEmail.setCc(Collections.singletonList(DEFAULT_USER));
        recipientEmail.setReplyTo("noOne@example.com");
        RECIPIENT = recipientEmail;
    }

    @Disabled
    @Test
    public void testSimpleEmail() {
        final MediaData mailBody = new MediaData();
        mailBody.setMediaType(MediaTypes.MEDIA_XTYPE_TEXT);
        mailBody.setText("How are you today?\n\nBest regards...");

        new CommonsEmailService().sendEmail(System.currentTimeMillis(), UUID.randomUUID(),
                new EmailMessage(RECIPIENT, "Hello world", mailBody, null, null, null), MODULE_CONFIG);
    }

    @Disabled
    @Test
    public void testHtmlEmail() {
        final MediaData mailBody = new MediaData();
        mailBody.setMediaType(MediaTypes.MEDIA_XTYPE_HTML);
        mailBody.setText("  <html>\n"
                + "             <body>\n"
                + "                 My <b>dear</b> friend John,\n"
                + "                 <p>\n"
                + "                 you should know that 28 &lt; 42.\n"
                + "             </body>\n"
                + "         </html>");

        final MediaData alternateBody = new MediaData();
        alternateBody.setMediaType(MediaTypes.MEDIA_XTYPE_TEXT);
        alternateBody.setText("My dear friend John,\nyou should know that 28 < 42\n");

        new CommonsEmailService().sendEmail(System.currentTimeMillis(), UUID.randomUUID(),
                new EmailMessage(RECIPIENT, "Hello John", mailBody, null, null, alternateBody), MODULE_CONFIG);
    }
}
