/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.core;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

public class T9tCoreException extends T9tException {
    private static final long serialVersionUID = -1258793470293665993L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = T9tConstants.EXCEPTION_OFFSET_CORE;
    private static final int OFFSET      = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;

    // Update status - start update (1-10)
    public static final int UPDATE_STATUS_ALREADY_IN_PROGRESS   = OFFSET + 1;
    public static final int UPDATE_STATUS_INVALID_STATE         = OFFSET + 2;
    public static final int UPDATE_STATUS_PREREQUISITES         = OFFSET + 3;

    // Update status - finish update (11-20)
    public static final int FINISH_UPDATE_MUST_BE_IN_PROGRESS   = OFFSET + 11;

    // additional values for release update sequences
    public static final int UPDATE_MISSING_IMPLEMENTATION       = OFFSET + 21;

    static {
        registerRange(CORE_OFFSET, false, T9tCoreException.class, ApplicationLevelType.FRAMEWORK, "t9t core extension module");

        registerCode(UPDATE_STATUS_ALREADY_IN_PROGRESS, "Ticket update already in progress.");
        registerCode(UPDATE_STATUS_INVALID_STATE, "Ticket isn't in valid state.");
        registerCode(UPDATE_STATUS_PREREQUISITES, "Not all prerequisite tickets are completed.");

        registerCode(FINISH_UPDATE_MUST_BE_IN_PROGRESS, "Ticket update must be in progress.");

        registerCode(UPDATE_MISSING_IMPLEMENTATION, "No updater implementation found for the specified ticketId.");
    }
}
