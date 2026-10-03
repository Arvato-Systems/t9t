/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.base.api.RequestParameters;

/**
 * Defines the methods to convert the name of a request object to the class name of the corresponding request handler,
 * and also to provide an instance of it. The default implementation does this by naming convention as well as caching.
 * Jdp is not used here.
 *
 * Also methods to overwrite an existing implementation are provided, this is used by caching algorithms, plugging into the cross module resolvers.
 */
public interface IRequestHandlerResolver {

    <RQ extends RequestParameters> IRequestHandler<RQ> getHandlerInstance(Class<RQ> requestClass);

    <RQ extends RequestParameters> void setHandlerInstance(Class<RQ> requestClass, IRequestHandler<RQ> newInstance);

    default int getSkippedComponentsForHandlerClassnameCandidates() {
        return 4; // default: skip the first 4 components of the package name
    }

    /**
     * Enumerates the fully qualified names of potential request handler classes for the given request class.
     * The candidates consist of the package name with an inserted "be" or "jpa" component after the first 4 components, up to before the last,
     * then followed by the simple name of the request, with suffix "Handler".
     * For example com.dummy.app.pkg.request.XyzRequest would have the candidates com.dummy.app.pkg.{be,jpa}.request.XyzRequestHandler.
     * If there are too few components in the package name, no candidates are returned.
     *
     * Customizations can override this method to provide additional candidates, e.g. by inserting a "custom" package component or similar.
     */
    default <RQ extends RequestParameters> List<String> getRequestHandlerClassnameCandidates(final Class<RQ> requestClass) {
        // default strategy: insert a ".be" after "com.arvato-systems.t9t.[a-z]*"
        final String packageName = requestClass.getPackageName();
        final String requestClassNameSuffix = "." + requestClass.getSimpleName() + "Handler";

        // Find the dot after the fourth component, or return an empty list if there are less than 5 components.
        int splitAt = 0; // initialize
        final int toSkip = getSkippedComponentsForHandlerClassnameCandidates();
        for (int component = 0; component < toSkip; component++) {
            splitAt = packageName.indexOf('.', splitAt + 1);
            if (splitAt < 0) {
                return List.of(); // less than 5 components, no candidates
            }
        }
        // construct the various candidates
        final List<String> candidates = new ArrayList<>(8);

        // Move the split right, one component at a time.
        do {
            final String part1 = packageName.substring(0, splitAt + 1); // the first part, including the dot
            final String part2 = packageName.substring(splitAt); // the second part, including the dot
            candidates.add(part1 + "be" + part2 + requestClassNameSuffix);
            candidates.add(part1 + "jpa" + part2 + requestClassNameSuffix);
            splitAt = packageName.indexOf('.', splitAt + 1);
        } while (splitAt > 0);

        return candidates;
    }
}
