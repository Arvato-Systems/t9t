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
package com.arvatosystems.t9t.tfi.general;

import java.text.SimpleDateFormat;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import com.arvatosystems.t9t.tfi.web.ZulUtils;

public class DateUtils {
    private static SimpleDateFormat dateFormat;
    private static SimpleDateFormat dateTimeFormat;

    public static final String getDate(LocalDate date) {
        return getDateFormat().format(date.toDate());
    }

    public static final String getDateTime(LocalDateTime dateTime) {
        return getDateTimeFormat().format(dateTime.toDate());
    }

    private static SimpleDateFormat getDateFormat() {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(ZulUtils.translate("com","dateFormat"));
        }
        return dateFormat;
    }

    private static SimpleDateFormat getDateTimeFormat() {
        if (dateTimeFormat == null) {
            dateTimeFormat = new SimpleDateFormat(ZulUtils.translate("com","datetimeFormat"));
        }
        return dateTimeFormat;
    }
}
