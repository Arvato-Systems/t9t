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
package com.arvatosystems.t9t.io.services;

import java.util.Locale;

import org.joda.time.DateTimeZone;

import com.arvatosystems.t9t.io.CsvConfigurationDTO;
import com.arvatosystems.t9t.io.CsvDateTimeStyleType;

import de.jpaw.bonaparte.core.CSVConfiguration;
import de.jpaw.bonaparte.core.CSVStyle;

public class CSVTools {
    private static CSVStyle t2b(CsvDateTimeStyleType t) {
        return t == null ? null : CSVStyle.factory(t.getToken());
    }

    public static CSVConfiguration getCsvConfiguration(CsvConfigurationDTO cfgDTO) {
        if (cfgDTO == null)
            return CSVConfiguration.CSV_DEFAULT_CONFIGURATION;
        else {
            // create a custom configuration
            Integer quote = cfgDTO.getQuote();

            return new CSVConfiguration(
                cfgDTO.getSeparator(),
                quote == null ? null : Character.valueOf((char)quote.intValue()),
                cfgDTO.getQuoteReplacement(),
                cfgDTO.getCtrlReplacement(),
                cfgDTO.getQuoteDates(),
                cfgDTO.getRemovePoint(),
                cfgDTO.getMapStart(),
                cfgDTO.getMapEnd(),
                cfgDTO.getArrayStart(),
                cfgDTO.getArrayEnd(),
                cfgDTO.getObjectStart(),
                cfgDTO.getObjectEnd(),
                cfgDTO.getBooleanTrue(),
                cfgDTO.getBooleanFalse(),
                cfgDTO.getLanguageCode()!= null? Locale.forLanguageTag(cfgDTO.getLanguageCode()): null,
                cfgDTO.getTimeZone() != null ? DateTimeZone.forID(cfgDTO.getTimeZone()): null,
                t2b(cfgDTO.getDayStyle()),
                t2b(cfgDTO.getTimeStyle()),
                cfgDTO.getCustomDayFormat(),
                cfgDTO.getCustomTimeFormat(),
                cfgDTO.getCustomTimeWithMsFormat(),
                cfgDTO.getCustomTsFormat(),
                cfgDTO.getCustomTsWithMsFormat(),
                cfgDTO.getZeroPadNumbers(),
                cfgDTO.getRightPadNumbers(),
                cfgDTO.getUseGrouping()
            );
        }
    }
}
