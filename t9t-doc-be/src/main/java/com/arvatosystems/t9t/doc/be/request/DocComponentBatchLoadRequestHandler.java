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
package com.arvatosystems.t9t.doc.be.request;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.JsonUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.DocComponentKey;
import com.arvatosystems.t9t.doc.request.DocComponentBatchLoadRequest;
import com.arvatosystems.t9t.doc.request.DocComponentCrudRequest;
import com.arvatosystems.t9t.doc.request.DocComponentResource;

import de.jpaw.bonaparte.core.CSVConfiguration;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.bonaparte.core.StringCSVParser;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;
import de.jpaw.json.JsonException;
import de.jpaw.json.JsonParser;

public class DocComponentBatchLoadRequestHandler extends AbstractRequestHandler<DocComponentBatchLoadRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocComponentBatchLoadRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final DocComponentBatchLoadRequest rq) {
        // create a writeable copy of the key
        final DocComponentKey mutableKey = rq.getKey().ret$MutableClone(false, false);
        boolean somethingLoaded = false;    // keep track if some data was loaded, to catch errors

        // part 1: CSV data
        final List<String> csv = rq.getCsv();
        if (csv != null && !csv.isEmpty()) {
            LOGGER.info("Creating DocComponents from CSV data: {} entries provided", csv.size());
            // create the CSV parser
            final CSVConfiguration.Builder builder = CSVConfiguration.CSV_DEFAULT_CONFIGURATION.builder();
            builder.usingSeparator("|");
            builder.usingQuoteCharacter(null);
            final StringCSVParser parser = new StringCSVParser(builder.build(), "");

            for (final String line : csv) {
                parser.setSource(line);
                final DocComponentResource record = parser.readObject(DocComponentResource.meta$$this, DocComponentResource.class);
                final String value = record.getValue();
                if (value != null && value.trim().length() > 0) {
                    record.setValue(JsonUtil.unescapeJson(value));
                }
                load(ctx, record, mutableKey);
            }
            somethingLoaded = true;  // it is enough that there was a file (despite it could be empty)
        }

        // part 2: structured (pre parsed) data
        final List<DocComponentResource> data = rq.getData();
        if (data != null && !data.isEmpty()) {
            LOGGER.info("Creating DocComponents from preparsed data: {} entries provided", data.size());
            for (final DocComponentResource record : data) {
                load(ctx, record, mutableKey);
            }
            somethingLoaded = true;
        }

        // part 3: JSON data
        final Object json = rq.getJson();
        if (json != null) {
            LOGGER.info("Creating DocComponents from JSON data");
            if (json instanceof List<?> jsonList) {
                load(ctx, jsonList, mutableKey, rq.getMultiLineJoin());
            } else {
                throw new JsonException(JsonException.JSON_SYNTAX, "Outer element must be an array");
            }
            somethingLoaded = true;
        }

        // part 4: JSON source
        final String jsonString = rq.getJsonString();
        if (jsonString != null) {
            LOGGER.info("Creating DocComponents from JSON source string");

            final JsonParser parser = new JsonParser(jsonString, true);
            final List<Object> objects = parser.parseArray();
            this.load(ctx, objects, mutableKey, rq.getMultiLineJoin());
            somethingLoaded = true;
        }
        // if nothing loaded at all, complain
        if (!somethingLoaded) {
            throw new T9tException(T9tException.ILLEGAL_REQUEST_PARAMETER, "no data provided");
        }

        // if in cluster mode, send a cache invalidation event
        executor.clearCache(ctx, DocComponentDTO.class.getSimpleName(), null);
        return ok();
    }

    protected void load(final RequestContext ctx, final DocComponentResource inData, final DocComponentKey baseKey) {
        baseKey.setDocumentId(inData.getKey().trim());
        LOGGER.debug("Loading doc component for {} in format {}", baseKey.getDocumentId(), inData.getType());

        // determine the mime type, either by name or by token
        final MediaType type = MediaType.valueOf(inData.getType().trim().toUpperCase());

        // create a CRUD merge
        final DocComponentDTO dto = new DocComponentDTO();
        final MediaData data = new MediaData();
        data.setMediaType(type);
        data.setText(inData.getValue());
        dto.setDocumentId(baseKey.getDocumentId());
        dto.setEntityId(baseKey.getEntityId());
        dto.setCountryCode(baseKey.getCountryCode());
        dto.setCurrencyCode(baseKey.getCurrencyCode());
        dto.setLanguageCode(baseKey.getLanguageCode());
        dto.setData(data);

        final DocComponentCrudRequest crudRequest = new DocComponentCrudRequest();
        crudRequest.setCrud(OperationType.MERGE);
        crudRequest.setData(dto);
        crudRequest.setNaturalKey(baseKey);

        executor.executeSynchronousAndCheckResult(ctx, crudRequest, CrudSurrogateKeyResponse.class);
    }

    protected Object needKey(final Map<?, ?> map, final String key) {
        final Object value = map.get(key);
        if (value == null) {
            throw new JsonException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, key);
        }
        return value;
    }

    protected void load(final RequestContext ctx, final List<?> objects, final DocComponentKey baseKey, final String joiner) {
        LOGGER.info("{} JSON entries provided", objects.size());

        for (final Object obj : objects) {
            if (obj instanceof Map<?, ?> objMap) {
                if (objMap.size() != 3) {
                    LOGGER.warn("Field count not as expected: found {} entries, expected 3", objMap.size());
                }
                final DocComponentResource inData = new DocComponentResource();
                inData.setKey(needKey(objMap, "key").toString().trim());
                inData.setType(needKey(objMap, "type").toString().trim());
                final Object fields = needKey(objMap, "text");
                if (fields instanceof List<?> fieldList) {
                    String value = null;
                    if (fieldList != null) {
                        value = "";
                        for (final Object field : fieldList) {
                            value += field.toString();
                            if (joiner != null) {
                                value += joiner;
                            }
                        }
                    }
                    inData.setValue(value);
                } else {
                    inData.setValue(fields.toString());
                }
                load(ctx, inData, baseKey);
            }
        }
    }
}
