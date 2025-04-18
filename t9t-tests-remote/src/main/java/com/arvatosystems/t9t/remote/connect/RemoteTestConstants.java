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
package com.arvatosystems.t9t.remote.connect;

public final class RemoteTestConstants {
    private RemoteTestConstants() {
    }

    // pathnames of t9t REST API endpoints
    public static final String REST_PATH_PING          = "ping";
    public static final String REST_PATH_AUTH_API_KEY  = "apikey";
    public static final String REST_PATH_AUTH_BASIC    = "userpw";
    public static final String REST_PATH_EMAI_DOCUMENT = "createAndEmailDocument";
    public static final String REST_PATH_RUN_REQUEST   = "run";
}
