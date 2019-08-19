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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;

/** Defines the methods to convert the name of a request object to the class name of the corresponding request handler,
 * and also to provide an instance of it. The default implementation does this by naming convention as well as caching.
 *  Jdp is not used here.
 *
 *   Also methods to overwrite an existing implementation are provided, this is used by caching algorithms, plugging into the cross module resolvers. */
public interface IRequestHandlerResolver {
    public static final String PREFIX = MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX + ".";
    public static final int PREFIX_LENGTH = PREFIX.length();

    public <RQ extends RequestParameters> IRequestHandler<RQ> getHandlerInstance(Class<RQ> requestClass);

    public <RQ extends RequestParameters> void setHandlerInstance(Class<RQ> requestClass, IRequestHandler<RQ> newInstance);


    // see also NoTenantCustomization
    default public <RQ extends RequestParameters> String getRequestHandlerClassname(Class<RQ> requestClass) {
        // default strategy: insert a ".be" after "com.arvato-systems.t9t.[a-z]*"
        String base = requestClass.getCanonicalName();
        if (base.startsWith(PREFIX)) {
            int nextDot = base.indexOf('.', PREFIX_LENGTH);
            if (nextDot > 0) {
                return base.substring(0, nextDot) + ".be" + base.substring(nextDot) + "Handler";
            }
        }
        return base + "Handler";
    }
}
