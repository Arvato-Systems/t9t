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
package com.arvatosystems.t9t.out.be.impl.poi.formatgenerator;

import java.io.IOException;

import com.arvatosystems.t9t.out.be.impl.formatgenerator.FoldableFormatGenerator;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.bonaparte.poi.ExcelComposer;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Dependent
@Named("XLS")
public class FormatGeneratorXls extends FoldableFormatGenerator<RuntimeException> {

    protected final ExcelComposer xlsComposer = new ExcelComposer();

    @Override
    protected void openHook() throws IOException {
        super.openHook();
        xlsComposer.newSheet("Sheet 1");
        writeTitles();
    }

    @Override
    protected MessageComposer<RuntimeException> getMessageComposer() {
        return xlsComposer;
    }

    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable record) throws IOException {
        foldingComposer.writeRecord(record);
    }

    @Override
    public void close() throws IOException {
        xlsComposer.closeSheet();
        xlsComposer.write(outputResource.getOutputStream());
    }
}
