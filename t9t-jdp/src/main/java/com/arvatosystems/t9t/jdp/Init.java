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
package com.arvatosystems.t9t.jdp;

import java.util.function.Consumer;

import org.reflections.Reflections;

import com.arvatosystems.t9t.init.InitContainers;

import de.jpaw.dp.Jdp;

public class Init {

    /** Initializes Jdp using defaults. */
    public static void initializeT9t() {
        initializeT9t(null);
    }

    /** Initializes Jdp with the possibility to alter the automatically assigned classes before initialization. */
    public static void initializeT9t(Consumer<Reflections []> callback) {
        Jdp.reset();
        // Jdp.excludePackagePrefix("java.");
        Jdp.includePackagePrefix("de.jpaw.");
        Jdp.includePackagePrefix("com.arvatosystems.");

        Reflections [] scannedPackages = InitContainers.initializeT9t();
        Jdp.scanClasses(scannedPackages);
        if (callback != null)
            callback.accept(scannedPackages);
        Jdp.runStartups(scannedPackages);
    }
}
