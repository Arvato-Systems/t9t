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
package com.arvatosystems.t9t.ai.openai;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

/**
 * exception class for all t9t OpenAI module specific exceptions.
 *
 */
public class T9tOpenAIException extends T9tException {
    private static final long serialVersionUID = -866589603331210L;

    private static final int CORE_OFFSET            = T9tConstants.EXCEPTION_OFFSET_OPENAI;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    private static final int OFFSET_TIMEOUT = (CL_TIMEOUT * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    private static final int OFFSET_ILE = (CL_INTERNAL_LOGIC_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // Error codes
    public static final int OPENAI_NOT_CONFIGURED                         = OFFSET          + 1;
    public static final int OPENAI_CONNECTION_PROBLEM                     = OFFSET_TIMEOUT  + 2;
    public static final int OPENAI_INVALID_REQUEST                        = OFFSET_ILE      + 3;
    public static final int OPENAI_METADATA_TOO_LARGE                     = OFFSET          + 4;
    public static final int OPENAI_METADATA_KEY_TOO_LONG                  = OFFSET          + 5;
    public static final int OPENAI_METADATA_VALUE_TOO_LONG                = OFFSET          + 6;
    public static final int OPENAI_METADATA_VALUE_WRONG_TYPE              = OFFSET          + 7;
    public static final int OPENAI_UNKNOWN_RUN_STATUS                     = OFFSET_ILE      + 8;
    public static final int OPENAI_EXPECTED_TOOL_OUTPUTS                  = OFFSET_ILE      + 9;
    public static final int OPENAI_QUOTA_EXCEEDED                         = OFFSET_TIMEOUT  + 10;
    public static final int OPENAI_ORGANIZATION                           = OFFSET          + 11;

    static {
        registerRange(CORE_OFFSET, false, T9tOpenAIException.class, ApplicationLevelType.FRAMEWORK, "t9t OpenAI interface");

        registerCode(OPENAI_NOT_CONFIGURED, "No connection data to OpenAI configured in server.xml");
        registerCode(OPENAI_CONNECTION_PROBLEM, "Connection with OpenAI failed");
        registerCode(OPENAI_INVALID_REQUEST, "Invalid parameters passed to OpenAI");
        registerCode(OPENAI_METADATA_TOO_LARGE, "Invalid parameters passed as metadata: Too many entries (max 16 allowed)");
        registerCode(OPENAI_METADATA_KEY_TOO_LONG, "Invalid parameters passed as metadata: key too long (max 16 chars allowed)");
        registerCode(OPENAI_METADATA_VALUE_TOO_LONG, "Invalid parameters passed as metadata: value too long (max 512 chars allowed)");
        registerCode(OPENAI_METADATA_VALUE_WRONG_TYPE, "Invalid parameters passed as metadata: value must be a string");
        registerCode(OPENAI_UNKNOWN_RUN_STATUS, "Thread run has an unknown status");
        registerCode(OPENAI_EXPECTED_TOOL_OUTPUTS, "Expected toolOutputs section in response of OpenAI");
        registerCode(OPENAI_QUOTA_EXCEEDED, "Quota exceeded");
        registerCode(OPENAI_ORGANIZATION, "Must be part of an organization or invalid API key");
    }
}
