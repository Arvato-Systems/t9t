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
package com.arvatosystems.t9t.email;

import com.arvatosystems.t9t.base.T9tException;

/**
 * This class contains all exception codes used in email module.
 */
public class T9tEmailException extends T9tException {
    private static final long serialVersionUID = -71286196818412432L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = 190000;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    public static final int SMTP_IMPLEMENTATION_MISSING      = OFFSET + 966;
    public static final int EMAIL_SEND_ERROR                 = OFFSET + 967;
    public static final int SMTP_ERROR                       = OFFSET + 968;
    public static final int MIME_MESSAGE_COMPOSITION_PROBLEM = OFFSET + 969;

    /**
     * static initialization of all error codes
     */
    static {
        codeToDescription.put(SMTP_IMPLEMENTATION_MISSING,      "Configured STMP backend implementation not available.");
        codeToDescription.put(EMAIL_SEND_ERROR,                 "Error occured during sending email.");
        codeToDescription.put(SMTP_ERROR,                       "Error occured during sending email. (SMTP layer)");
        codeToDescription.put(MIME_MESSAGE_COMPOSITION_PROBLEM, "Problem composing MIME message.");
    }
}
