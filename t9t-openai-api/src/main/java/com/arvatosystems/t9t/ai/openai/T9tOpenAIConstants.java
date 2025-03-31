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

public final class T9tOpenAIConstants {
    private T9tOpenAIConstants() { }

    public static final String UPLINK_KEY_OPENAI  = "OPENAI";

    public static final String OPENAI_HTTP_AUTH   = "Bearer ";
    public static final String OPENAI_HTTP_CLIENT = "OpenAI-Organization";
    public static final String OPENAI_HTTP_BETA   = "OpenAI-Beta";

    public static final int OPENAI_MAX_TIME = 20;           // the maximum time (in seconds) we wait for chat completion or thread completion
    public static final int OPENAI_MAX_POLL_DURATION = 20;  // the polling interval (in milliseconds) to check if an assistant run result is available

    /** Validation constants for metadata. */
    public static final int MAX_METADATA_ENTRIES = 16;
    public static final int MAX_METADATA_KEY_LENGTH = 16;
    public static final int MAX_METADATA_VALUE_LENGTH = 512;
}
