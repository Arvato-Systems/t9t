/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.doc.be.api;

import java.util.HashMap;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.api.FormatDocumentRequest;
import com.arvatosystems.t9t.doc.api.FormatDocumentResponse;
import com.arvatosystems.t9t.doc.api.TemplateType;
import com.arvatosystems.t9t.doc.services.IDocFormatter;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;

public class FormatDocumentRequestHandler extends AbstractRequestHandler<FormatDocumentRequest> {
    private final IDocFormatter docFormatter = Jdp.getRequired(IDocFormatter.class);

    @Override
    public FormatDocumentResponse execute(final RequestContext ctx, final FormatDocumentRequest request) throws Exception {
        final HashMap<String, MediaData> attachments = request.getBinaryAsAttachments() ? new HashMap<String, MediaData>(16) : null;
        final Long sharedTenantRef = ctx.tenantMapping.getSharedTenantRef(DocComponentDTO.class$rtti());
        final MediaData result = docFormatter.formatDocument(ctx.tenantId, sharedTenantRef, TemplateType.DOCUMENT_ID, request.getDocumentId(),
          request.getDocumentSelector(), request.getTimeZone(), request.getData(), attachments);

        final FormatDocumentResponse resp = new FormatDocumentResponse();
        resp.setMediaType(result.getMediaType());
        resp.setText(result.getText());
        resp.setAttachments(attachments);
        return resp;
    }
}
