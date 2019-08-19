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
package com.arvatosystems.t9t.doc.be.request

import com.arvatosystems.t9t.base.JsonUtil
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.DocComponentDTO
import com.arvatosystems.t9t.doc.DocComponentKey
import com.arvatosystems.t9t.doc.request.DocComponentBatchLoadRequest
import com.arvatosystems.t9t.doc.request.DocComponentCrudRequest
import com.arvatosystems.t9t.doc.request.DocComponentResource
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.CSVConfiguration
import de.jpaw.bonaparte.core.MessageParserException
import de.jpaw.bonaparte.core.StringCSVParser
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.dp.Inject
import de.jpaw.json.JsonException
import de.jpaw.json.JsonParser
import java.util.List
import java.util.Map

@AddLogger
class DocComponentBatchLoadRequestHandler extends AbstractRequestHandler<DocComponentBatchLoadRequest> {

    @Inject IExecutor executor

    def protected void load(RequestContext ctx, DocComponentResource inData, DocComponentKey baseKey) {
        baseKey.documentId = inData.key.trim
        LOGGER.debug("Loading doc component for {} in format {}", baseKey.documentId, inData.type)

        // determine the mime type, either by name or by token
        val type = MediaType.valueOf(inData.type.trim.toUpperCase)

        // create a CRUD merge
        val dto = new DocComponentDTO => [
            documentId      = baseKey.documentId
            entityId        = baseKey.entityId
            countryCode     = baseKey.countryCode
            currencyCode    = baseKey.currencyCode
            languageCode    = baseKey.languageCode
            data            = new MediaData => [
                mediaType   = type
                text        = inData.value
            ]
        ]
        val crudRequest = new DocComponentCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = baseKey
        ]
        executor.executeSynchronousAndCheckResult(ctx, crudRequest, CrudSurrogateKeyResponse)
    }

    def protected needKey(Map<?, Object> map, String key) {
        val value = map.get(key)
        if (value === null)
            throw new JsonException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD, key)
        return value
    }

    def protected void load(RequestContext ctx, List<Object> objects, DocComponentKey baseKey, String joiner) {
        LOGGER.info("{} JSON entries provided", objects.size)

        for (obj : objects) {
            if (obj instanceof Map) {
                if (obj.size != 3)
                    LOGGER.warn("Field count not as expected: found {} entries, expected 3", obj.size)
                val inData = new DocComponentResource
                inData.key = obj.needKey("key").toString
                inData.type = obj.needKey("type").toString
                val fields = obj.needKey("text")
                if (fields instanceof List)
                    inData.value = if (joiner === null) fields.join else fields.join(joiner)
                else
                    inData.value = fields.toString
                load(ctx, inData, baseKey)
            }
        }
    }

    override ServiceResponse execute(RequestContext ctx, DocComponentBatchLoadRequest rq)  {
        // create a writeable copy of the key
        val mutableKey = rq.key.ret$MutableClone(false, false)

        // part 1: CSV data
        if (rq.csv.nullOrEmpty) {
            LOGGER.info("No CSV data provided")
        } else {
            LOGGER.info("Creating DocComponents from CSV data: {} entries provided", rq.csv.size)
            // create the CSV parser
            val builder = CSVConfiguration.CSV_DEFAULT_CONFIGURATION.builder
            builder.usingSeparator("|")
            builder.usingQuoteCharacter(null)
            val parser = new StringCSVParser(builder.build, "")

            for (line : rq.csv) {
                parser.source = line
                val record = parser.readObject(DocComponentResource.meta$$this, DocComponentResource)
                // unescape JSON
                record.value = JsonUtil.unescapeJson(record.value)
                load(ctx, record, mutableKey)
            }
        }

        // part 2: structured (pre parsed) data
        if (rq.data.nullOrEmpty) {
            LOGGER.info("No pre-parsed data provided")
        } else {
            LOGGER.info("Creating DocComponents from preparsed data: {} entries provided", rq.data.size)
            for (record: rq.data) {
                load(ctx, record, mutableKey)
            }
        }

        // part 3: JSON data
        if (rq.json === null) {
            LOGGER.info("No JSON data provided")
        } else {
            LOGGER.info("Creating DocComponents from JSON data")
            val objects = rq.json
            if (objects instanceof List)
                load(ctx, objects, mutableKey, rq.multiLineJoin)
            else
                throw new JsonException(JsonException.JSON_SYNTAX, "Outer element must be an array")
        }

        // part 4: JSON source
        if (rq.jsonString === null) {
            LOGGER.info("No JSON source provided")
        } else {
            LOGGER.info("Creating DocComponents from JSON source string")

            val parser = new JsonParser(rq.jsonString, true)
            val objects = parser.parseArray()
            load(ctx, objects, mutableKey, rq.multiLineJoin)
        }

        // if in cluster mode, send a cache invalidation event
        executor.clearCache(DocComponentDTO.simpleName, null);
        return ok
    }
}
