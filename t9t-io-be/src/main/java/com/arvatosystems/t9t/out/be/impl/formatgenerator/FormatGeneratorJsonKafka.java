/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;

/**
 * Creates JSON output with @PQON information where required. (depending on parameter1)
 *
 * enums are output as ordinal and token by default.
 * enums are output using instance names if SinkCfgDTo.genericParameter2 = "1"
 *
 */
@Dependent
@Named("JSON-Kafka")
public class FormatGeneratorJsonKafka extends AbstractFormatGenerator {
    final StringBuilder buff = new StringBuilder(4000);
    final JsonComposer jsonComposer = new JsonComposer(buff);

    @Override
    protected void openHook() throws IOException, ApplicationException {
        jsonComposer.setWritePqonInfo("1".equals(sinkCfg.getGenericParameter1()));
        if ("1".equals(sinkCfg.getGenericParameter2())) {
            jsonComposer.setWriteEnumOrdinals(false);
            jsonComposer.setWriteEnumTokens(false);
        }
        super.openHook();
    }

    @Override
    public void generateData(int recordNo, int mappedRecordNo, long recordId, String partitionKey, String recordKey, BonaPortable record) throws IOException, ApplicationException {
        buff.setLength(0);
        jsonComposer.writeObject(record);
        outputResource.write(partitionKey, recordKey, buff.toString());
    }

    @Override
    public void close() throws IOException, ApplicationException {
    }
}
