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

    private static final int CORE_OFFSET    = T9tConstants.EXCEPTION_OFFSET_AI;
    private static final int OFFSET         = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;
    private static final int OFFSET_TIMEOUT = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_TIMEOUT;
    private static final int OFFSET_ILE     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_INTERNAL_LOGIC_ERROR;
    private static final int OFFSET_ERR     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_DATABASE_ERROR;

    // Error codes
    public static final int NO_ASSISTANT                = OFFSET + 100;
    public static final int UNKNOWN_AI_TOOL             = OFFSET + 101;
    public static final int AI_TOOL_NO_PERMISSION       = OFFSET + 102;
    public static final int MCP_SERIALIZATION_ERROR     = OFFSET_ERR + 103;
    public static final int INVALID_PROMPT_NAME         = OFFSET + 104;
    public static final int MISSING_REQUIRED_ARGUMENT   = OFFSET + 105;
    public static final int AI_PROMPT_NO_PERMISSION     = OFFSET + 106;
    public static final int TOOLS_NOT_AVAILABLE         = OFFSET + 107;
    public static final int PROMPTS_NOT_AVAILABLE       = OFFSET + 108;
    public static final int PROMPTS_MISSING_PARAMETERS  = OFFSET + 109;

    static {
        registerRange(CORE_OFFSET, false, T9tAiException.class, ApplicationLevelType.FRAMEWORK, "t9t general AI integration layer");

        registerCode(NO_ASSISTANT,              "No assistant configured");
        registerCode(UNKNOWN_AI_TOOL,           "No AI tool of such name");
        registerCode(AI_TOOL_NO_PERMISSION,     "No permission to run specified AI tool");
        registerCode(MCP_SERIALIZATION_ERROR,   "Problem serializing MCP result to JSON");
        registerCode(INVALID_PROMPT_NAME,       "Invalid prompt name");
        registerCode(MISSING_REQUIRED_ARGUMENT, "Required argument is missing");
        registerCode(AI_PROMPT_NO_PERMISSION,   "No permission to get specific AI prompt");
        registerCode(TOOLS_NOT_AVAILABLE,       "Tools cannot be fetched");
        registerCode(PROMPTS_NOT_AVAILABLE,     "Prompts cannot be fetched");
        registerCode(PROMPTS_MISSING_PARAMETERS, "Prompt parameters missing");
    }
}
