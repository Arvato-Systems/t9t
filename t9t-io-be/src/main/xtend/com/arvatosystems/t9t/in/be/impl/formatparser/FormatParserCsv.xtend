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
package com.arvatosystems.t9t.in.be.impl.formatparser

import com.arvatosystems.t9t.in.be.impl.AbstractTextFormatConverter
import com.arvatosystems.t9t.in.services.IInputSession
import com.arvatosystems.t9t.io.CsvConfigurationDTO
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.services.CSVTools
import com.arvatosystems.t9t.server.services.IStatefulServiceSession
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortableClass
import de.jpaw.bonaparte.core.CSVConfiguration
import de.jpaw.bonaparte.core.StaticMeta
import de.jpaw.bonaparte.core.StringCSVParser
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named
import java.util.Map

@Dependent
@Named("CSV")
@AddLogger
class FormatParserCsv extends AbstractTextFormatConverter {
    protected CSVConfiguration csvCfg
    protected StringCSVParser parser

    override open(IInputSession inputSession, DataSinkDTO cfg, IStatefulServiceSession session, Map<String, Object> params, BonaPortableClass<?> baseBClass) {
        super.open(inputSession, cfg, session, params, baseBClass)
        csvCfg = CSVTools.getCsvConfiguration(cfg.csvConfigurationRef as CsvConfigurationDTO);
        parser = new StringCSVParser(csvCfg, "")
        if (Boolean.TRUE == cfg.nationalNumberFormat)
            parser.setNationalBigDecimal();
    }

    override process(String textLine) {
        parser.source= textLine
//        val dto = parser.readRecord
        val dto = parser.readObject(StaticMeta.OUTER_BONAPORTABLE_FOR_CSV, baseBClass.bonaPortableClass)
        inputSession.process(dto)
    }
}
