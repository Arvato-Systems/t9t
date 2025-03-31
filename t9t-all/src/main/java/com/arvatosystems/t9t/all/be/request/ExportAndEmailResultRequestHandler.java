/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.all.be.request;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.all.request.ExportAndEmailResultRequest;
import com.arvatosystems.t9t.authc.api.GetMyUserDataRequest;
import com.arvatosystems.t9t.authc.api.GetUserDataResponse;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.doc.api.NewDocumentResponse;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.io.request.RetrieveMediaDataRequest;
import com.arvatosystems.t9t.io.request.RetrieveMediaDataResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.jpaw.dp.Jdp;

public class ExportAndEmailResultRequestHandler extends AbstractRequestHandler<ExportAndEmailResultRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportAndEmailResultRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, ExportAndEmailResultRequest request) throws Exception {
        // obtain the user data
        final GetUserDataResponse userData = executor.executeSynchronousAndCheckResult(ctx, new GetMyUserDataRequest(), GetUserDataResponse.class);

        // get a mutable copy of the search request
        final SearchCriteria searchReq = request.getSearchRequest().ret$MutableClone(true, true); // async invocation freezes it, and export needs a mutable

        // next, execute the search request
        final ReadAllResponse raResp = executor.executeSynchronousAndCheckResult(ctx, searchReq, ReadAllResponse.class);
        if (raResp.getSinkRef() == null) {
            LOGGER.warn("Export to email without any export target, by user {}", ctx.userId);
            return ok();
        }

        // obtain the data from the sink
        final RetrieveMediaDataRequest rmdReq = new RetrieveMediaDataRequest();
        rmdReq.setSinkRef(raResp.getSinkRef());
        final RetrieveMediaDataResponse rmdResp = executor.executeSynchronousAndCheckResult(ctx, rmdReq, RetrieveMediaDataResponse.class);
        // now email the result
        final String emailTo = request.getTargetEmailAddress() != null ? request.getTargetEmailAddress() : userData.getUserData().getEmailAddress();
        if (emailTo == null) {
            LOGGER.warn("Export to email without any email address given, for user {}", ctx.userId);
            return ok(); // no email address given, nothing to do!
        }
        final NewDocumentRequest ndReq = new NewDocumentRequest();
        ndReq.setDocumentId(request.getDocumentTemplateId());
        ndReq.setDocumentSelector(DocConstants.GENERIC_DOCUMENT_SELECTOR);
        ndReq.setData(ImmutableMap.of("u", userData.getUserData()));
        ndReq.setRecipientList(List.of(new RecipientEmail(ImmutableList.of(emailTo))));
        ndReq.setAttachments(ImmutableList.of(rmdResp.getMediaData()));
        final NewDocumentResponse ndResp = executor.executeSynchronousAndCheckResult(ctx, ndReq, NewDocumentResponse.class);
        return ndResp;
    }
}
