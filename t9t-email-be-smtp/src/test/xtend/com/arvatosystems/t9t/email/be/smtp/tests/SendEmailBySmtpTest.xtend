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
package com.arvatosystems.t9t.email.be.smtp.tests

import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.be.smtp.impl.SmtpEmailService
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import java.util.UUID
import org.junit.Ignore
import org.junit.Test
import com.arvatosystems.t9t.email.api.EmailMessage

class SendEmailBySmtpTest {
    private static final String DEFAULT_USER     = "xxx@online.de"
    private static final String DEFAULT_PASSWORD = "yyy"

    private static final EmailModuleCfgDTO MODULE_CONFIG = new EmailModuleCfgDTO => [
        implementation      = "SMTP"
        smtpServerTransport = "smtp"
        smtpServerAddress   = "smtp.1und1.de"
        smtpServerUserId    = DEFAULT_USER
        smtpServerPassword  = DEFAULT_PASSWORD
        smtpServerPort      = 587
        smtpServerTls       = Boolean.TRUE
    ]
    private static final RecipientEmail RECIPIENT = new RecipientEmail => [
        to                  = #[ "jpaw@online.de" ]
        from                = DEFAULT_USER
        cc                  = #[ DEFAULT_USER ]
        replyTo             = "noOne@example.com"
    ]

    @Ignore
    @Test
    def public void testSimpleEmail() {

        new SmtpEmailService().sendEmail(
            System.currentTimeMillis,
            UUID.randomUUID,
            new EmailMessage(
                RECIPIENT,
                "Hello world",
                new MediaData => [
                    mediaType = MediaTypes.MEDIA_XTYPE_TEXT
                    text = "How are you today?\n\nBest regards..."
                ],
                null, null, null
            ),
            MODULE_CONFIG
        )
    }

    @Ignore
    @Test
    def public void testHtmlEmail() {

        new SmtpEmailService().sendEmail(
            System.currentTimeMillis,
            UUID.randomUUID,
            new EmailMessage(
                RECIPIENT,
                "Hello John",
                new MediaData => [
                    mediaType = MediaTypes.MEDIA_XTYPE_HTML
                    text = '''
                        <html>
                            <body>
                                My <b>dear</b> friend John,
                                <p>
                                you should know that 28 &lt; 42.
                            </body>
                        </html>
                    '''
                ],
                null, null,
                new MediaData => [
                    mediaType = MediaTypes.MEDIA_XTYPE_TEXT
                    text = "My dear friend John,\nyou should know that 28 < 42\n"
                ]
            ),
            MODULE_CONFIG
        )
    }
}
