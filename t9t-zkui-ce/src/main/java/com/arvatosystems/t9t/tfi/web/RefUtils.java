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
package com.arvatosystems.t9t.tfi.web;

public class RefUtils {
    /**
     * Factor to multiply the value obtained from sequences with.
     */
    public static final long KEY_FACTOR = 10000L;

    /**
     * Offset to be added to scaled keys generated in the backup location.
     */
    public static final int OFFSET_BACKUP_LOCATION = 5000;

    /** Offsets of sequences which are unscaled (don't contain a RTTI). */
    public static final int OFFSET_UNSCALED_T9T           = 5000;
    public static final int OFFSET_UNSCALED_APPLICATION   = 6000;
    public static final int OFFSET_UNSCALED_CUSTOMIZATION = 7000;

    /**
     * Retrieves the run time type information from a generated key.
     *
     * @param id
     *            The key to extract the RTTI from.
     * @return the RTTI
     */
    public static int getRtti(long id) {
        int rttiPlusLocation = (int) (id % KEY_FACTOR);
        return rttiPlusLocation >= OFFSET_BACKUP_LOCATION ? rttiPlusLocation - OFFSET_BACKUP_LOCATION : rttiPlusLocation;
    }

}
