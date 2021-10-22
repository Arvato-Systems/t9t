/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base;

/**
 * Implementations provide the default URL (protocol / host / port / base path) of the most widely used remote for IRemoteConnection.
 * Usually that is the t9t server. Implementations can use environment, system parameters or JNDI to obtain the values.
 * These defaults are used for
 * - ZK UI
 * - API gateway
 * - Remote tests
 */
public interface IRemoteDefaultUrlRetriever {
    /** Get the path for regular requests. */
    String getDefaultRemoteUrl();

    /** Get the path for login requests. This is derived from the regular path. */
    default String getDefaultRemoteUrlLogin() {
        return getDefaultRemoteUrlLogin(getDefaultRemoteUrl());
    }

    /** Get the path for login requests. This is derived from the regular path. */
    static String getDefaultRemoteUrlLogin(String mainUrl) {
        final int lastSlash = mainUrl.lastIndexOf('/');
        return mainUrl.substring(0, lastSlash) + "/login";
    }
}
