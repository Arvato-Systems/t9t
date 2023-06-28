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
package com.arvatosystems.t9t.rest.endpoints;

import static com.arvatosystems.t9t.base.T9tUtil.nvl;

import java.util.Collections;

import com.arvatosystems.t9t.doc.DocConstants;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.rest.services.IT9tRestEndpoint;
import com.arvatosystems.t9t.rest.services.IT9tRestProcessor;
import com.arvatosystems.t9t.xml.GenericResult;
import com.arvatosystems.t9t.xml.doc.CreateAndEmailDocument;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

/**
 * Create document and send email endpoint.
 */
@Path("createAndEmailDocument")
@Tag(name = "t9t")
@Singleton
public class CreateAndEmailDocumentRestResource implements IT9tRestEndpoint {
    // private static final Logger LOGGER = LoggerFactory.getLogger(CreateAndEmailDocumentRestResource.class);

    protected final IT9tRestProcessor restProcessor = Jdp.getRequired(IT9tRestProcessor.class);

    @Operation(
        summary = "Create document and send by email",
        description = "Generates a document for the provided template, and sends it via email to the recipient list.",
        responses = {
            @ApiResponse(
              description = "Request passed.",
              content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = GenericResult.class))),
            @ApiResponse(responseCode = "400", description = "Bad request.")}
    )
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public void createAndEmailDocument(@Context final HttpHeaders httpHeaders, @Suspended final AsyncResponse resp, final CreateAndEmailDocument params) {
        validatePayload(params);
        final NewDocumentRequest rq = new NewDocumentRequest();
        final DocumentSelector sel = new DocumentSelector();
        final RecipientEmail re = new RecipientEmail();
        re.setTo(params.getEmailRecipients());
        sel.setLanguageCode(nvl(params.getLanguageCode(), DocConstants.DEFAULT_LANGUAGE_CODE));
        sel.setCurrencyCode(nvl(params.getCurrencyCode(), DocConstants.DEFAULT_CURRENCY_CODE));
        sel.setCountryCode (nvl(params.getCountryCode(),  DocConstants.DEFAULT_COUNTRY_CODE));
        sel.setEntityId    (nvl(params.getEntityId(),     DocConstants.DEFAULT_ENTITY_ID));
        rq.setDocumentId(params.getDocumentId());
        rq.setDocumentSelector(sel);
        rq.setData(params.getData());
        rq.setTimeZone(params.getTimeZone());
        rq.setRecipientList(Collections.singletonList(re));
        restProcessor.performAsyncBackendRequest(httpHeaders, resp, rq, "POST /createAndEmailDocument");
    }
}
