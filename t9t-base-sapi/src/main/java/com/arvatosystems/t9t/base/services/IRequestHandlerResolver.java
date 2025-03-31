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

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.cfg.Packages;

import jakarta.annotation.Nonnull;

/** Defines the methods to convert the name of a request object to the class name of the corresponding request handler,
 * and also to provide an instance of it. The default implementation does this by naming convention as well as caching.
 *  Jdp is not used here.
 *
 *   Also methods to overwrite an existing implementation are provided, this is used by caching algorithms, plugging into the cross module resolvers. */
public interface IRequestHandlerResolver {
    String PREFIX = MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX + ".";

    <RQ extends RequestParameters> IRequestHandler<RQ> getHandlerInstance(Class<RQ> requestClass);

    <RQ extends RequestParameters> void setHandlerInstance(Class<RQ> requestClass, IRequestHandler<RQ> newInstance);


    // see also NoTenantCustomization
    default <RQ extends RequestParameters> List<String> getRequestHandlerClassnameCandidates(final Class<RQ> requestClass) {
        // default strategy: insert a ".be" after "com.arvato-systems.t9t.[a-z]*"
        final String requestClassName = requestClass.getCanonicalName();
        final List<String> candidates = new ArrayList<>(4);
        checkPrefix(candidates, requestClassName, PREFIX);
        if (candidates.isEmpty()) {
            // check extra packages (if any)
            Packages.walkExtraPackages((prefix, packageName) -> checkPrefix(candidates, requestClassName, packageName));
        }
        // Check for additional candidates: Find "request" package name component, construct from there
        final int pos = requestClassName.indexOf(".request.");
        if (pos > 0) {
            // found some candidates
            final String part1 = requestClassName.substring(0, pos);
            final String part2 = requestClassName.substring(pos);
            final String additionalCandidate1 = part1 + ".be" + part2 + "Handler";
            final String additionalCandidate2 = part1 + ".jpa" + part2 + "Handler";
            if (!candidates.contains(additionalCandidate1)) {
                candidates.add(additionalCandidate1);
            }
            if (!candidates.contains(additionalCandidate2)) {
                candidates.add(additionalCandidate2);
            }
        }
        return candidates;
    }

    /** Adds potential class names for request handlers to the provided result list. */
    default void checkPrefix(@Nonnull final List<String> candidates, @Nonnull final String base, @Nonnull final String packagePrefix) {
        if (base.startsWith(packagePrefix)) {
            final int nextDot = base.indexOf('.', packagePrefix.length());
            if (nextDot > 0) {
                // found some candidates
                final String part1 = base.substring(0, nextDot);
                final String part2 = base.substring(nextDot);
                candidates.add(part1 + ".be" + part2 + "Handler");
                candidates.add(part1 + ".jpa" + part2 + "Handler");
            }
        }
    }
}
