/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.be.camel

import de.jpaw.annotations.AddLogger
import java.util.Map
import org.apache.camel.Component
import org.apache.camel.impl.DefaultComponent

@AddLogger
class CamelT9tComponent extends DefaultComponent implements Component {

    override protected createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        val endpoint = new CamelT9tEndpoint(uri, this, remaining)
        setProperties(endpoint, parameters)
        return endpoint
    }
}
