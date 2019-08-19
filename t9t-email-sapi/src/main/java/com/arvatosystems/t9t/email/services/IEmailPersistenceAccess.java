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
package com.arvatosystems.t9t.email.services;

import java.util.UUID;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;

/** Defines the communication layer between the backend modules (business logic / persistence layer). */
public interface IEmailPersistenceAccess {
    public static final EmailModuleCfgDTO DEFAULT_MODULE_CFG = new EmailModuleCfgDTO(
    );

    // MediaData               getEmailAttachment    (Long emailRef, Integer attachmentNo);
    // List<MediaData>         getEmailAttachments   (Long emailRef);
    void                    persistEmail(long emailRef, UUID messageId, RequestContext ctx, EmailMessage msg, boolean sendSpooled, boolean storeEmail);
}
