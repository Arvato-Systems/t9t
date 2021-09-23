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
package com.arvatosystems.t9t.tfi.general;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.arvatosystems.t9t.tfi.web.ZulUtils;

public class DateUtils {
    private static DateTimeFormatter dateFormat;
    private static DateTimeFormatter dateTimeFormat;

    public static final String getDate(LocalDate date) {
        return date.format(getDateFormat());
    }

    public static final String getDateTime(LocalDateTime dateTime) {
        return dateTime.format(getDateTimeFormat());
    }

    private static DateTimeFormatter getDateFormat() {
        if (dateFormat == null) {
            dateFormat = DateTimeFormatter.ofPattern(ZulUtils.translate("com","dateFormat"));
        }
        return dateFormat;
    }

    private static DateTimeFormatter getDateTimeFormat() {
        if (dateTimeFormat == null) {
            dateTimeFormat = DateTimeFormatter.ofPattern(ZulUtils.translate("com","datetimeFormat"));
        }
        return dateTimeFormat;
    }
}
