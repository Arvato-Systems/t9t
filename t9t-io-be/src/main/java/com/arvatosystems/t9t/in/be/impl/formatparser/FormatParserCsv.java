/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.be.impl.formatparser;

import java.util.Map;

import com.arvatosystems.t9t.in.be.impl.AbstractTextFormatConverter;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.CSVTools;
import com.arvatosystems.t9t.io.CsvConfigurationDTO;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.CSVConfiguration;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.bonaparte.core.StringCSVParser;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Dependent
@Named("CSV")
public class FormatParserCsv extends AbstractTextFormatConverter {
    protected CSVConfiguration csvCfg;
    protected StringCSVParser parser;

    @Override
    public void open(final IInputSession inputSession, final Map<String, Object> params,
            final BonaPortableClass<?> baseBClass) {
        super.open(inputSession, params, baseBClass);
        final DataSinkDTO cfg = inputSession.getDataSinkDTO();

        csvCfg = CSVTools.getCsvConfiguration((CsvConfigurationDTO) cfg.getCsvConfigurationRef());
        parser = new StringCSVParser(csvCfg, "");
        if (Boolean.TRUE.equals(cfg.getNationalNumberFormat())) {
            parser.setNationalBigDecimal();
        }
    }

    @Override
    public void process(final String textLine) {
        parser.setSource(textLine);
        final BonaPortable dto = parser.readObject(StaticMeta.OUTER_BONAPORTABLE_FOR_CSV, baseBClass.getBonaPortableClass());
        inputSession.process(dto);
    }
}
