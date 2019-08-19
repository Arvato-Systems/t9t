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
package com.arvatosystems.t9t.auth;

import com.arvatosystems.t9t.base.T9tException;

public class T9tAuthException extends T9tException {
    private static final long serialVersionUID = -3356256622929031419L;

    private static final int CORE_OFFSET = 21000;
    private static final int OFFSET_VALIDATION_ERROR     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_VALIDATION_ERROR;

    public static final int PASSWORD_VALIDATION_FAILED   = OFFSET_VALIDATION_ERROR + 1;
    public static final int PERMISSION_VALIDATION_FAILED = OFFSET_VALIDATION_ERROR + 20;

    static {
        codeToDescription.put(PASSWORD_VALIDATION_FAILED,   "Validation for the given password failed");
        codeToDescription.put(PERMISSION_VALIDATION_FAILED, "The user is not permitted to do the action");
    }
}
