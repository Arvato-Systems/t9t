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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;

/** API between the request handler and the internal implementations of the low level sender.
 * In case of issues, the sender returns a ServiceResponse which outlines the error code and any details.
 *
 * Implementations are queried by JDP qualifier.
 *
 */
public interface IEmailSender {
    ServiceResponse sendEmail(
            Long messageRef,
            UUID messageId,
            EmailMessage msg,
            EmailModuleCfgDTO configuration);
}
