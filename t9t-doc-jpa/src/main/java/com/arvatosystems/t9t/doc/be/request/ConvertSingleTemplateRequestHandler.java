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
package com.arvatosystems.t9t.doc.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.T9tDocException;
import com.arvatosystems.t9t.doc.request.ConvertSingleTemplateRequest;
import com.arvatosystems.t9t.doc.request.ConvertSingleTemplateResponse;
import com.arvatosystems.t9t.doc.services.ITemplateConversion;

import de.jpaw.dp.Jdp;

public class ConvertSingleTemplateRequestHandler extends AbstractRequestHandler<ConvertSingleTemplateRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConvertSingleTemplateRequestHandler.class);

    private final ITemplateConversion conversionService = Jdp.getRequired(ITemplateConversion.class);

    @Override
    public ConvertSingleTemplateResponse execute(final RequestContext ctx, final ConvertSingleTemplateRequest rq) throws Exception {
        final String newTemplate = conversionService.convertTemplate(rq.getDocumentId(), rq.getTemplate());
        if (newTemplate.length() >= ConvertSingleTemplateResponse.meta$$template.getLength()) {
            LOGGER.error("Conversion of template {} would exceed the maximum allowed size", rq.getDocumentId());
            throw new T9tException(T9tDocException.CONVERSION_EXCEEDS_MAX_TEMPLATE_SIZE, rq.getDocumentId());
        }
        final ConvertSingleTemplateResponse resp = new ConvertSingleTemplateResponse();
        resp.setTemplate(newTemplate);
        return resp;
    }
}
