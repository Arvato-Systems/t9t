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
package com.arvatosystems.t9t.base.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Conversion between dates and timestamps, which take the tenant's time zone into account.
 */
public interface IDateTimeService {

    /**
     * Returns the calendar day which is now, measured in the tenant's time zone.
     *
     * @param tenantId          the tenant which specifies the time zone
     * @param when              the instant to be converted
     * @return                  the local date for the specified instant for an observer located in the tenant's time zone
     */
    default LocalDate getCurrentDay(final String tenantId) {
        return toLocalDate(tenantId, Instant.now());
    }

    /**
     * Converts a given Instant to a calendar day, measured in the tenant's time zone.
     *
     * @param tenantId          the tenant which specifies the time zone
     * @param when              the instant to be converted
     * @return                  the local date for the specified instant for an observer located in the tenant's time zone
     */
    LocalDate toLocalDate(String tenantId, Instant when);

    /**
     * Converts a given LocalDateTime (assumed in UTC) to a calendar day, measured in the tenant's time zone.
     *
     * @param tenantId          the tenant which specifies the time zone
     * @param when              the local date time in UTC to be converted
     * @return                  the local date for the specified instant for an observer located in the tenant's time zone
     */
    LocalDate toLocalDate(String tenantId, LocalDateTime when);

    /**
     * Provides the instant in time which corresponds to the beginning of a given calendar day in the tenant's time zone.
     *
     * @param tenantId          the tenant which specifies the time zone
     * @param when              the day to be converted
     * @return                  the instant in time which corresponds to the beginning of a given calendar day in the tenant's time zone
     */
    Instant toInstantAtStartOfDay(String tenantId, LocalDate when);

    /**
     * Provides the timestamp in UTC which corresponds to the beginning of a given calendar day in the tenant's time zone.
     *
     * @param tenantId          the tenant which specifies the time zone
     * @param when              the day to be converted
     * @return                  the corresponding UTC timestamp
     */
    LocalDateTime toLocalDateTimeAtStartOfDay(String tenantId, LocalDate when);
}
