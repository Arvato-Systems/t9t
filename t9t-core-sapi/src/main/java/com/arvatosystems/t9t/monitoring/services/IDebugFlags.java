/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.monitoring.services;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.arvatosystems.t9t.base.services.RequestContext;

public interface IDebugFlags {
    /** Retrieve all flags. */
    ConcurrentMap<String, String> getAllFlags();
    /** Retrieve all flags. Use if the RequestContext is available. */
    ConcurrentMap<String, String> getAllFlags(RequestContext ctx);

    /** Retrieve a specific flag. Returns null if the setting does not exist. */
    String getFlag(String flag);
    /** Retrieve a specific flag. Returns null if the setting does not exist. Use if the RequestContext is available. */
    String getFlag(RequestContext ctx, String flag);

    /** sets or clears flags. */
    void setFlags(RequestContext ctx, Map<String, String> newFlags) throws Exception;
}
