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

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IHighRiskSituationNotificationService;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.email.api.RecipientEmail;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DocHighRiskSituationNotificationService implements IHighRiskSituationNotificationService {

    @Override
    public void notifyChange(final RequestContext ctx, final String changeType, final String userId, final String name, final String emailAddress, final String newEmailAddress) {

        final NewDocumentRequest docRequest = new NewDocumentRequest();
        docRequest.setDocumentId(T9tConstants.DOCUMENT_ID_HIGH_RISK_SITUATION);
        final Map<String, String> data = new HashMap<>(8);
        data.put("changeType", changeType);
        data.put("userId", userId);
        data.put("name", name);
        data.put("emailAddress", emailAddress);
        data.put("newEmailAddress", newEmailAddress);
        docRequest.setData(data);
        docRequest.setDocumentSelector(DocConstants.GENERIC_DOCUMENT_SELECTOR);
        final List<String> emailList = List.of(emailAddress);
        docRequest.setRecipientList(Collections.singletonList(new RecipientEmail(emailList)));
        final IExecutor executor = Jdp.getRequired(IExecutor.class); // cannot inject due to cyclic dependency
        executor.executeAsynchronous(ctx, docRequest);
    }
}
