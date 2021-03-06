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
package com.arvatosystems.t9t.out.be.impl.poi.formatgenerator;

import java.io.IOException;

import com.arvatosystems.t9t.out.be.impl.formatgenerator.FoldableFormatGenerator;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.bonaparte.poi.ExcelXComposer;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;

@Dependent
@Named("XLSX")
public class FormatGeneratorXlsx extends FoldableFormatGenerator<RuntimeException> {

    protected final ExcelXComposer xlsComposer = new ExcelXComposer();

    @Override
    protected void openHook() throws IOException, ApplicationException {
        super.openHook();
        xlsComposer.newSheet("Sheet 1");
        writeTitles();
    }

    @Override
    protected MessageComposer<RuntimeException> getMessageComposer() {
        return xlsComposer;
    }

    @Override
    public void generateData(int recordNo, int mappedRecordNo, long recordId, String partitionKey, String recordKey, BonaPortable record) throws IOException, ApplicationException {
        foldingComposer.writeRecord(record);
    }

    @Override
    public void close() throws IOException {
        xlsComposer.closeSheet();
        xlsComposer.write(outputResource.getOutputStream());
    }
}
