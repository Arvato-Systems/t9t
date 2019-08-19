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
package com.arvatosystems.t9t.base.be.lifecycle;


import com.arvatosystems.t9t.base.be.execution.RequestContextScope;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;

// this is required before the JPA layer initializes, because it injects the contextprovider
@Startup(1800)
public class Init01800Basics {
    public static void onStartup() {

        // create a custom scope for the request context. This is for the getters!
//        Jdp.registerWithCustomProvider(RequestContext.class, RequestContextWithScope.getProvider());
        Jdp.registerWithCustomProvider(RequestContext.class, Jdp.getRequired(RequestContextScope.class));
    }
}
