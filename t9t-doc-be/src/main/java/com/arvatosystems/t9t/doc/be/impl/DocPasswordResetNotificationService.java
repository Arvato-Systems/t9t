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
package com.arvatosystems.t9t.doc.be.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IPasswordResetNotificationService;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.email.api.RecipientEmail;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DocPasswordResetNotificationService implements IPasswordResetNotificationService {
    private static final String RESET_PASSWORD_DOC_ID = "passwordReset";

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public boolean isPasswordResetAllowed(final RequestContext ctx, final String userId) {
        return true;
    }

    @Override
    public void notifyUser(final RequestContext ctx, final String emailAddress, final String newPassword) {
        // The password is emailed to the user
        final NewDocumentRequest emailRequest = new NewDocumentRequest();
        emailRequest.setDocumentId(RESET_PASSWORD_DOC_ID);
        final Map<String, String> data = new HashMap<>();
        data.put("password", newPassword);
        emailRequest.setData(data);
        final DocumentSelector selector = new DocumentSelector();
        selector.setEntityId("-");
        selector.setCountryCode("XX");
        selector.setCurrencyCode("XXX");
        selector.setLanguageCode(ctx.internalHeaderParameters.getLanguageCode() == null ? "en" : ctx.internalHeaderParameters.getLanguageCode());
        emailRequest.setDocumentSelector(selector);
        final List<String> emailList = List.of(emailAddress);
        emailRequest.setRecipientList(Collections.singletonList(new RecipientEmail(emailList)));
        executor.executeSynchronous(ctx, emailRequest);
    }
}
