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
package com.arvatosystems.t9t.plugins.services;

import com.arvatosystems.t9t.base.services.RequestContext;

/**
 * Defines the API to call a plugin which provides a single method.
 * The method has been defined such newer releases of the core can enhance the IN as well as OUT data structures,
 * and technically, older plugins can continue to work
 * (provided the caller supports that in these cases only a fraction of the fields on the OUT class has been populated.
 **/

public interface IGenericCompatiblePluginMethod<I, O> extends PluginMethod {
    /** Execute an API method. In case of problems, a T9tException will be thrown. */
    void execute(RequestContext ctx, I in, O out);
}
