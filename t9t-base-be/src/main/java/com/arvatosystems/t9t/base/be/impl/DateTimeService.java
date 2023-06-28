/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import com.arvatosystems.t9t.base.services.IDateTimeService;
import com.arvatosystems.t9t.base.services.ITimeZoneProvider;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

/**
 * Implementation of the methods defined in IDateTimeService.
 */
@Singleton
public class DateTimeService implements IDateTimeService {
    protected final ITimeZoneProvider timeZoneProvider = Jdp.getRequired(ITimeZoneProvider.class);

    @Override
    public LocalDate toLocalDate(final String tenantId, final Instant when) {
        final ZoneId timeZone = timeZoneProvider.getTimeZoneOfTenant(tenantId);
        return LocalDate.ofInstant(when, timeZone);
    }

    @Override
    public LocalDate toLocalDate(final String tenantId, final LocalDateTime when) {
        return toLocalDate(tenantId, when.toInstant(ZoneOffset.UTC));  // convert the timestamp to an instant and use the existing method for that
    }

    @Override
    public Instant toInstantAtStartOfDay(final String tenantId, final LocalDate when) {
        final ZoneId timeZone = timeZoneProvider.getTimeZoneOfTenant(tenantId);
        return LocalDateTime.of(when, LocalTime.MIDNIGHT).atZone(timeZone).toInstant();
    }

    @Override
    public LocalDateTime toLocalDateTimeAtStartOfDay(final String tenantId, final LocalDate when) {
        return LocalDateTime.ofInstant(toInstantAtStartOfDay(tenantId, when), ZoneOffset.UTC);  // reduce it to the existing method for the instant
    }
}
