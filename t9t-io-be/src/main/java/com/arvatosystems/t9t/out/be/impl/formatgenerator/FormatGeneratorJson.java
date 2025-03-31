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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.io.IOException;
import java.io.OutputStreamWriter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/**
 * Creates JSON output with @PQON information where required.
 */
@Dependent
@Named("JSON")
public class FormatGeneratorJson extends FoldableFormatGenerator<IOException> {

    protected OutputStreamWriter osw = null;
    protected JsonComposer jsonComposer = null;

    @Override
    protected MessageComposer<IOException> getMessageComposer() {
        return jsonComposer;
    }

    @Override
    protected void openHook() throws IOException {
        osw = new OutputStreamWriter(outputResource.getOutputStream(), encoding);
        jsonComposer = new JsonComposer(osw);

        final boolean writeNulls  = Boolean.TRUE.equals(sinkCfg.getJsonWriteNulls());
        jsonComposer.setWriteNulls(writeNulls);

        final boolean writePqonInfo  = Boolean.TRUE.equals(sinkCfg.getJsonWritePqon());
        jsonComposer.setWritePqonInfo(writePqonInfo);

        final boolean writeTokens = Boolean.TRUE.equals(sinkCfg.getJsonUseEnumTokens());
        jsonComposer.setWriteEnumOrdinals(writeTokens);
        jsonComposer.setWriteEnumTokens(writeTokens);

        jsonComposer.startTransmission();
        super.openHook();
    }

    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable record) throws IOException {
        foldingComposer.writeRecord(record);
    }

    @Override
    public void close() throws IOException {
        jsonComposer.terminateTransmission();
        osw.flush();  // no result without the flush()
    }
}
