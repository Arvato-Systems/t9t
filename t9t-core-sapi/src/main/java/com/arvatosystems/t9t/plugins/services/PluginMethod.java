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
package com.arvatosystems.t9t.plugins.services;

import java.lang.reflect.ParameterizedType;

import com.arvatosystems.t9t.base.services.RequestContext;

/**
 * Defines the API to call a method of a plugin. A single plugin can support multiple of these APIs.
 * For every provided extension point, a specialized interface which specifies I and O will be defined,
 * allowing to define the plugins in a type safe manner.
 **/
public interface PluginMethod<I,O> {
    /** Execute an API method. In case of problems, a T9tException will be thrown. */
    void execute(RequestContext ctx, I in, O out);
}
