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
package com.arvatosystems.t9t.auth;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

public class T9tAuthException extends T9tException {
    private static final long serialVersionUID = -3356256622929031419L;

    private static final int CORE_OFFSET = T9tConstants.EXCEPTION_OFFSET_AUTH;
    private static final int OFFSET_VALIDATION_ERROR     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_VALIDATION_ERROR;
    private static final int OFFSET_DECLINE              = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_DENIED;

    public static final int PASSWORD_VALIDATION_FAILED   = OFFSET_VALIDATION_ERROR + 1;
    public static final int PERMISSION_VALIDATION_FAILED = OFFSET_VALIDATION_ERROR + 20;
    public static final int LOGIN_FAILED                 = OFFSET_VALIDATION_ERROR + 30;
    public static final int INVALID_TENANT_ID            = OFFSET_VALIDATION_ERROR + 31;
    public static final int INVALID_USER_ID              = OFFSET_VALIDATION_ERROR + 32;

    public static final int PASSWORD_RESET_NOT_ALLOWED   = OFFSET_DECLINE + 41;

    static {
        registerRange(CORE_OFFSET, false, T9tAuthException.class, ApplicationLevelType.FRAMEWORK, "t9t authentication module");

        registerCode(PASSWORD_VALIDATION_FAILED,   "Validation for the given password failed");
        registerCode(PERMISSION_VALIDATION_FAILED, "The user is not permitted to do the action");
        registerCode(LOGIN_FAILED, "Login failed");
        registerCode(INVALID_TENANT_ID, "The tenantId does not match the allowed pattern (only letters, digits and the underscore)");
        registerCode(INVALID_USER_ID, "The userId does not match the allowed pattern (only letters, digits, the underscore and at most one dot)");
        registerCode(PASSWORD_RESET_NOT_ALLOWED, "Password reset not supported for this server / user");
    }
}
