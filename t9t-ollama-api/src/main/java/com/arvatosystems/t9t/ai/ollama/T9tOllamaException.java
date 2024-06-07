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
package com.arvatosystems.t9t.ai.ollama;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

/**
 * exception class for all t9t Ollama module specific exceptions.
 *
 */
public class T9tOllamaException extends T9tException {
    private static final long serialVersionUID = -866589603131610L;

    private static final int CORE_OFFSET            = T9tConstants.EXCEPTION_OFFSET_OLLAMA;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    private static final int OFFSET_TIMEOUT = (CL_TIMEOUT * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    private static final int OFFSET_ILE = (CL_INTERNAL_LOGIC_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // Error codes
    public static final int OLLAMA_NOT_CONFIGURED                         = OFFSET          + 1;
    public static final int OLLAMA_CONNECTION_PROBLEM                     = OFFSET_TIMEOUT  + 2;
    public static final int OLLAMA_INVALID_REQUEST                        = OFFSET_ILE      + 3;

    static {
        registerRange(CORE_OFFSET, false, T9tOllamaException.class, ApplicationLevelType.FRAMEWORK, "t9t OpenAI interface");

        registerCode(OLLAMA_NOT_CONFIGURED, "No connection data to Ollama server configured in server.xml");
        registerCode(OLLAMA_CONNECTION_PROBLEM, "Connection with Ollama server failed");
        registerCode(OLLAMA_INVALID_REQUEST, "Invalid parameters passed to Ollama server");
    }
}
