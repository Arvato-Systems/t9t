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
package com.arvatosystems.t9t.base.services;

import jakarta.annotation.Nonnull;

public interface IRefGenerator {
    /**
     * Factor to multiply the value obtained from sequences with.
     */
    long KEY_FACTOR = 10000L;

    /**
     * Offset to be added to scaled keys generated in the backup location.
     */
    int OFFSET_BACKUP_LOCATION = 5000;

    /** Offsets of sequences which are unscaled (don't contain a RTTI). */
    int OFFSET_UNSCALED_T9T           = 5000;
    int OFFSET_UNSCALED_APPLICATION   = 6000;
    int OFFSET_UNSCALED_CUSTOMIZATION = 7000;

//    /**
//     * Returns a valid technical Id which is guaranteed to be unique for all keys obtained through this method.
//     * Deprecated. Use <code>generateRef(String tablename, int rttiOffset)</code> instead.
//     *
//     * @param rttiOffset an offset for run time type information
//     *
//     * @return The new generated key value
//     */
//    @Deprecated
//    long generateRef(int rttiOffset);

    /**
     * Returns a valid technical Id which is guaranteed to be unique for all keys obtained through this method.
     *
     * @param tabename   the SQL name of the table for which the primary key should be used
     * @param rttiOffset an offset for run time type information
     *
     * @return The new generated key value
     */
    long generateRef(@Nonnull String tablename, int rttiOffset);

    /**
     * Retrieves the run time type information from a generated key.
     *
     * @param id
     *            The key to extract the RTTI from.
     * @return the RTTI
     */
    default int getRtti(final long id) {
        final int rttiPlusLocation = (int) (id % KEY_FACTOR);
        return rttiPlusLocation >= OFFSET_BACKUP_LOCATION ? rttiPlusLocation - OFFSET_BACKUP_LOCATION : rttiPlusLocation;
    }
    /**
     * Returns a valid technical Id which is only scaled by the location offset and does not contain the RTTI. Therefore, Refs returned by this method will
     * overlap. These should be used if counters should be small. A uniform internal caching of 10 is used.
     *
     * The rttiOffset should be in range
     *
     * 500x for refs required by the t9t application server
     *
     * 600x for refs required by the main application
     *
     * 700x for refs required by customizations
     *
     * @param rttiOffset
     *            An offset for run time type information
     * @return The new generated key value
     */
    long generateUnscaledRef(int rttiOffset);
}
