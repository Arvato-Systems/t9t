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

import com.arvatosystems.t9t.io.CSVTools;
import com.arvatosystems.t9t.io.CsvConfigurationDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.FixedWidthComposer;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Dependent
@Named("FIXED-WIDTH")
public class FormatGeneratorFixedWidth extends FoldableFormatGenerator<IOException> {

    protected OutputStreamWriter osw = null;
    protected FixedWidthComposer csvComposer = null;

    @Override
    protected MessageComposer<IOException> getMessageComposer() {
        return csvComposer;
    }

    @Override
    protected void openHook() throws IOException {
        final CsvConfigurationDTO csvCfg = (CsvConfigurationDTO)sinkCfg.getCsvConfigurationRef();
        osw = new OutputStreamWriter(outputResource.getOutputStream(), encoding);
        csvComposer = new FixedWidthComposer(osw, CSVTools.getCsvConfiguration(csvCfg));
        csvComposer.startTransmission();
        writeTitles();
        super.openHook();
    }

    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable record) throws IOException {
        foldingComposer.writeRecord(record);
    }

    @Override
    public void close() throws IOException {
        csvComposer.terminateTransmission();
        osw.flush();
    }
}
