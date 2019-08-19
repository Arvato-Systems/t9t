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
package com.arvatosystems.t9t.doc.be.api;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.DocComponentDTO
import com.arvatosystems.t9t.doc.api.FormatDocumentRequest
import com.arvatosystems.t9t.doc.api.FormatDocumentResponse
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.services.IDocFormatter
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Inject
import java.util.HashMap

class FormatDocumentRequestHandler extends AbstractRequestHandler<FormatDocumentRequest>  {

    @Inject IDocFormatter docFormatter

    override execute(RequestContext ctx, FormatDocumentRequest request) throws Exception {
        val attachments = if (request.binaryAsAttachments) new HashMap<String, MediaData>(16);
        val sharedTenantRef = ctx.tenantMapping.getSharedTenantRef(DocComponentDTO.class$rtti)
        val result = docFormatter.formatDocument(sharedTenantRef, TemplateType.DOCUMENT_ID, request.documentId, request.documentSelector, request.timeZone, request.data, attachments)
        return new FormatDocumentResponse => [
            mediaType       = result.mediaType
            text            = result.text
            it.attachments  = attachments
        ]
    }
}
