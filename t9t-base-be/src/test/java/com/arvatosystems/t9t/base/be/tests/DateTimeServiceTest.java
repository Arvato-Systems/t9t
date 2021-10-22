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
package com.arvatosystems.t9t.base.be.tests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.be.impl.DateTimeService;
import com.arvatosystems.t9t.base.services.IDateTimeService;
import com.arvatosystems.t9t.base.services.ITimeZoneProvider;

import de.jpaw.dp.Jdp;

public class DateTimeServiceTest {

    @Test
    public void testDefaultHandlerName() {

        // prep:
        Jdp.bindInstanceTo(tenantRef -> ZoneId.of("Europe/Berlin"), ITimeZoneProvider.class);

        final IDateTimeService dateTimeService = new DateTimeService();

        final LocalDate when = LocalDate.of(2021, 10, 17);
        // get the timestamp in UTC
        LocalDateTime timestampUTC = dateTimeService.toLocalDateTimeAtStartOfDay(T9tConstants.GLOBAL_TENANT_REF42, when);
        Assertions.assertEquals(when.getDayOfMonth()-1, timestampUTC.getDayOfMonth(), "expect UTC date to be one day before");
        Assertions.assertEquals(22, timestampUTC.getHour(), "expect UTC time to be 2 hours back");
        
        // convert back 3 timestamps:
        LocalDateTime before = timestampUTC.minusHours(3L);
        LocalDateTime after = timestampUTC.plusHours(3L);

        // test the other conversion direction
        Assertions.assertEquals(when, dateTimeService.toLocalDate(T9tConstants.GLOBAL_TENANT_REF42, timestampUTC),
                "expect to get back original date");
        Assertions.assertEquals(when, dateTimeService.toLocalDate(T9tConstants.GLOBAL_TENANT_REF42, after),
                "expect to get back original date some hours later");
        Assertions.assertEquals(when.minusDays(1L), dateTimeService.toLocalDate(T9tConstants.GLOBAL_TENANT_REF42, before),
                "expect to get previous date some hours before");
    }
}
