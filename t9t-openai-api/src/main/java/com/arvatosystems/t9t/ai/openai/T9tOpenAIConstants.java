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
