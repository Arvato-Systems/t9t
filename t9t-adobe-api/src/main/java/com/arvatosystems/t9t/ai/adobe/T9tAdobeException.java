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
package com.arvatosystems.t9t.ai.adobe;

import com.arvatosystems.t9t.base.T9tException;

/**
 * Exception codes for Adobe Firefly integration.
 */
public class T9tAdobeException extends T9tException {
    private static final long serialVersionUID = 3751964851862341L;

    private static final int CORE_OFFSET = 27000;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    public static final int ADOBE_NOT_CONFIGURED         = OFFSET + 1;
    public static final int ADOBE_HTTP_ERROR             = OFFSET + 2;
    public static final int ADOBE_TIMEOUT                = OFFSET + 3;
    public static final int ADOBE_INVALID_RESPONSE       = OFFSET + 4;
    public static final int ADOBE_JOB_FAILED             = OFFSET + 5;

    static {
        registerRange(OFFSET, false, T9tAdobeException.class, ApplicationLevelType.FRAMEWORK, "Adobe Firefly");

        registerCode(ADOBE_NOT_CONFIGURED,        "Adobe Firefly is not configured");
        registerCode(ADOBE_HTTP_ERROR,            "Adobe Firefly HTTP error");
        registerCode(ADOBE_TIMEOUT,               "Adobe Firefly request timeout");
        registerCode(ADOBE_INVALID_RESPONSE,      "Adobe Firefly invalid response");
        registerCode(ADOBE_JOB_FAILED,            "Adobe Firefly job failed");
    }
}
