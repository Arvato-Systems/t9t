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

import com.arvatosystems.t9t.all.request.MigrationTBE1053Request;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;

import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;

public class MigrationTBE1053RequestHandler extends AbstractMigrationRequestHandler<MigrationTBE1053Request> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExportAndEmailResultRequestHandler.class);
    private static final String TICKET_ID = "TBE-1053";
    private static final String SEQUENCE_ID = "V6.5_2023-07-05";

    private final IDataSinkEntityResolver dataSinkResolver = Jdp.getRequired(IDataSinkEntityResolver.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, MigrationTBE1053Request request) throws Exception {
        startUpdate(ctx, TICKET_ID, SEQUENCE_ID, false, "Use specific fields for data sink configuration instead of genericParameter1/2");
        convertDataSinks();
        finishUpdate(ctx, TICKET_ID);
        return ok();
    }

    private void convertDataSinks() {
        final List<DataSinkEntity> dataSinks = dataSinkResolver.readAll(false);
        for (final DataSinkEntity ds: dataSinks) {
            convertDataSink(ds);
        }
    }

    private void convertDataSink(final DataSinkEntity ds) {
        // the UI export data sink is identified by ID, it does not have a specific type
        final String id = ds.getDataSinkId();
        if (T9tConstants.DATA_SINK_ID_UI_EXPORT.equals(id)) {
            LOGGER.info("Setting writeHeaders for data sink {}", id);
            ds.setWriteHeaderRow(Boolean.TRUE);
            return;
        }

        final String commFormat = ds.getCommFormatType().getBaseEnum() == MediaType.USER_DEFINED ? ds.getCommFormatName() : ds.getCommFormatType().getToken();
        if (commFormat == null) {
            LOGGER.error("No user defined format given for data sink {}, skipping", id);
            return;
        }
        switch (commFormat) {
        case "XLS":
        case "XLSX":
            // both wrote a header row by default in the past - now it is only if configured
            ds.setWriteHeaderRow(Boolean.TRUE);
            break;
        case "CSV":
            ds.setWriteHeaderRow("1".equals(ds.getGenericParameter1()));
            break;
        case "JSON":
        case "JSON-Kafka":
            ds.setJsonWritePqon("1".equals(ds.getGenericParameter1()));
            ds.setJsonUseEnumTokens(!"1".equals(ds.getGenericParameter2()));  // default was to use tokens!
            break;
        case "JSONJackson":
            ds.setJsonWriteNulls("NULLs".equals(ds.getGenericParameter1()));
            break;
        default:
            return;  // no conversion
        }
        LOGGER.info("Data sink {} of type {} configured to use writeHeader={}, writePqon={}, useEnumTokens={}, writeNulls={}",
                id, commFormat, ds.getWriteHeaderRow(), ds.getJsonWritePqon(), ds.getJsonUseEnumTokens(), ds.getJsonWriteNulls());
    }
}
