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
package com.arvatosystems.t9t.out.be.impl.output.camel;

import org.apache.camel.builder.RouteBuilder;

/**
 * Abstract base class for all route builders used for generating additional camel routes from an extension module. All derived classes are instantiated at container start and added as route builder
 * to the camel context.
 */
public abstract class AbstractExtensionCamelRouteBuilder extends RouteBuilder {

}
