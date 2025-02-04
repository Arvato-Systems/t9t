/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.io.be.camel.service;

import org.apache.camel.CamelContext;

import com.arvatosystems.t9t.io.DataSinkDTO;

public interface ICamelService {
    void addRoutes(DataSinkDTO dataSink);
    void removeRoutes(DataSinkDTO dataSink);
    void startRoute(DataSinkDTO dataSink);

    /** Invoked before the Camel context is created. This can be used to set backwards compatibility flags. */
    void initBeforeContextCreation();

    /** Invoked to initialize cluster specific services, for example k8s. */
    void initializeClusterService(CamelContext camelContext);

    /** Invoked to initialize the configured routes. */
    void initializeRoutes(CamelContext camelContext);
    void initAfterContextCreation(CamelContext camelContext);
}
