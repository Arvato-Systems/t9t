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
package com.arvatosystems.t9t.ariba;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

public class AribaException extends T9tException {

    private static final long serialVersionUID = 8429309564894896786L;

    private static final int CORE_OFFSET                = T9tConstants.EXCEPTION_OFFSET_AI;
    private static final int OFFSET                     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;

    public static final int ARIBA_EXPORT_ERROR                          = OFFSET + 990;
    public static final int ARIBA_UNEXPECTED_RESULT_COLUMN_NUMBER       = OFFSET + 991;

    static {
        registerRange(CORE_OFFSET, false, T9tException.class, ApplicationLevelType.FRAMEWORK, "t9t framework core");

        registerCode(ARIBA_EXPORT_ERROR, "Error exporting metrics to ariba");
        registerCode(ARIBA_UNEXPECTED_RESULT_COLUMN_NUMBER, "Unexpected number of columns in the result set of an ariba view");
    }
}
