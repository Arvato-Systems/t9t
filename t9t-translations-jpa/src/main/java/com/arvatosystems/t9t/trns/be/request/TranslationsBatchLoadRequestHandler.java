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
package com.arvatosystems.t9t.trns.be.request;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.trns.TextCategory;
import com.arvatosystems.t9t.trns.TranslationsDTO;
import com.arvatosystems.t9t.trns.TranslationsKey;
import com.arvatosystems.t9t.trns.request.TranslationsBatchLoadRequest;
import com.arvatosystems.t9t.trns.request.TranslationsCrudRequest;
import com.arvatosystems.t9t.trns.request.TranslationsResource;

import de.jpaw.bonaparte.core.CSVConfiguration;
import de.jpaw.bonaparte.core.StringCSVParser;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;

public class TranslationsBatchLoadRequestHandler extends AbstractRequestHandler<TranslationsBatchLoadRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranslationsBatchLoadRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final TranslationsBatchLoadRequest rq) {
        boolean somethingLoaded = false; // keep track if some data was loaded, to catch errors

        final List<String> csv = rq.getCsv();
        if (!csv.isEmpty()) {
            LOGGER.info("Creating Translations from CSV data: {} entries provided", csv.size());
            // create the CSV parser
            final CSVConfiguration.Builder builder = CSVConfiguration.CSV_DEFAULT_CONFIGURATION.builder();
            builder.usingSeparator(rq.getDelimiter());
            builder.usingQuoteCharacter(null);
            final StringCSVParser parser = new StringCSVParser(builder.build(), "");

            for (final String line : csv) {
                parser.setSource(line);
                final TranslationsResource record = parser.readObject(TranslationsResource.meta$$this, TranslationsResource.class);

                load(ctx, rq.getCategory(), record);
            }
            somethingLoaded = true;
        }

        // if nothing loaded at all, complain
        if (!somethingLoaded) {
            throw new T9tException(T9tException.ILLEGAL_REQUEST_PARAMETER, "no data provided");
        }

        // if in cluster mode, send a cache invalidation event
        executor.clearCache(ctx, TranslationsDTO.class.getSimpleName(), null);
        return ok();
    }

    protected void load(final RequestContext ctx, final TextCategory category, final TranslationsResource inData) {
        final TranslationsKey baseKey = new TranslationsKey(category, inData.getQualifier(), inData.getId(), inData.getLanguageCode());
        LOGGER.debug("Loading translation for key {}", baseKey);

        // create a CRUD merge
        final TranslationsDTO dto = new TranslationsDTO();
        dto.setCategory(baseKey.getCategory());
        dto.setQualifier(baseKey.getQualifier());
        dto.setId(baseKey.getId());
        dto.setLanguageCode(baseKey.getLanguageCode());
        dto.setText(inData.getText());

        final TranslationsCrudRequest crudRequest = new TranslationsCrudRequest();
        crudRequest.setCrud(OperationType.MERGE);
        crudRequest.setData(dto);
        crudRequest.setNaturalKey(baseKey);

        executor.executeSynchronousAndCheckResult(ctx, crudRequest, CrudSurrogateKeyResponse.class);
    }
}
