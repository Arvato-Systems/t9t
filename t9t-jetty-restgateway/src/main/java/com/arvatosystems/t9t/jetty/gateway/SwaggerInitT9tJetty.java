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
package com.arvatosystems.t9t.jetty.gateway;

import com.arvatosystems.t9t.jetty.impl.SwaggerInit;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import io.swagger.v3.oas.models.info.Info;

@Specializes
@Singleton
public class SwaggerInitT9tJetty extends SwaggerInit {
    @Override
    protected Info createRestApiInfoForSwagger() {
        return super.createRestApiInfoForSwagger().version("t9t API 6.2.0"); // this is the version of most recent change of API, not the application version
    }
}
