/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.email.jpa.impl;

import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.jpa.entities.EmailModuleCfgEntity;
import com.arvatosystems.t9t.email.jpa.persistence.IEmailModuleCfgEntityResolver;
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver;
import de.jpaw.dp.Singleton;

@Singleton
public class EmailModuleCfgDtoResolver extends AbstractModuleConfigResolver<EmailModuleCfgDTO, EmailModuleCfgEntity> implements IEmailModuleCfgDtoResolver {
    private static final EmailModuleCfgDTO DEFAULT_MODULE_CFG = new EmailModuleCfgDTO(
            null,                          // Json z
            "SMTP",                        // implementation:          currently supported: SMTP, SES, VERTX
            "smtp",                        // smtpServerTransport      default to "smtp"
            "cmail.servicemail24.de",      // smtpServerAddress;
            25,                            // smtpServerPort
            null,                          // smtpServerUserId;
            null,                          // smtpServerPassword
            null,                          // smtpServerTls
            null                           // defaultReturnPath
    );

    public EmailModuleCfgDtoResolver() {
        super(IEmailModuleCfgEntityResolver.class);
    }

    @Override
    public EmailModuleCfgDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
