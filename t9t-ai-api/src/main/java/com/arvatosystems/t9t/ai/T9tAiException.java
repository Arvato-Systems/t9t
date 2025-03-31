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
package com.arvatosystems.t9t.ai;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

/**
 * exception class for all t9t OpenAI module specific exceptions.
 *
 */
public class T9tAiException extends T9tException {
    private static final long serialVersionUID = -866589612331210L;

    private static final int CORE_OFFSET            = T9tConstants.EXCEPTION_OFFSET_AI;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    private static final int OFFSET_TIMEOUT = (CL_TIMEOUT * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    private static final int OFFSET_ILE = (CL_INTERNAL_LOGIC_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // Error codes
    public static final int NO_ASSISTANT               = OFFSET + 100;

    static {
        registerRange(CORE_OFFSET, false, T9tAiException.class, ApplicationLevelType.FRAMEWORK, "t9t general AI integration layer");

        registerCode(NO_ASSISTANT, "No asistant configured");
    }
}
