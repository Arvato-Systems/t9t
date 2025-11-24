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

/**
 * Constants for Adobe Firefly integration.
 */
public final class T9tAdobeConstants {
    private T9tAdobeConstants() { }

    public static final String UPLINK_KEY_ADOBE                     = "Adobe";

    public static final String ADOBE_DEFAULT_MODEL_VERSION          = "image3";
    public static final String ADOBE_DEFAULT_CUSTOM_MODEL_VERSION   = "image3_custom";
    public static final String ADOBE_HTTP_AUTH_PREFIX               = "Bearer ";  // optional with BCP
    public static final String ADOBE_HEADER_X_API_KEY               = "x-api-key";
    public static final int ADOBE_DEFAULT_WIDTH                     = 2048;
    public static final int ADOBE_DEFAULT_HEIGHT                    = 2048;
}
