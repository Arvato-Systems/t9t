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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.io.IOException;

import com.arvatosystems.t9t.out.be.IThreadSafeFormatGenerator;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;

/**
 * Creates JSON output with @PQON information where required.
 */
@Dependent
@Named("JSON-Kafka")
public class FormatGeneratorJsonKafka extends AbstractFormatGenerator implements IThreadSafeFormatGenerator {
    private boolean writeNulls = false;
    private boolean writePqonInfo = false;
    private boolean writeTokens = false;

    @Override
    protected void openHook() throws IOException, ApplicationException {
        writeNulls  = Boolean.TRUE.equals(sinkCfg.getJsonWriteNulls());
        writePqonInfo  = Boolean.TRUE.equals(sinkCfg.getJsonWritePqon());
        writeTokens = Boolean.TRUE.equals(sinkCfg.getJsonUseEnumTokens());
        super.openHook();
    }

    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable record) throws IOException {
        final StringBuilder buff = new StringBuilder(4000);
        final JsonComposer jsonComposer = new JsonComposer(buff);
        jsonComposer.setWriteNulls(writeNulls);
        jsonComposer.setWritePqonInfo(writePqonInfo);
        jsonComposer.setWriteEnumOrdinals(writeTokens);
        jsonComposer.setWriteEnumTokens(writeTokens);
        jsonComposer.writeObject(record);
        outputResource.write(partitionKey, recordKey, buff.toString());
        jsonComposer.close();
    }

    @Override
    public void close() throws IOException, ApplicationException {
    }
}
