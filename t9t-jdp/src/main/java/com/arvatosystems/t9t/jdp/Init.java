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
package com.arvatosystems.t9t.jdp;

import java.util.function.Consumer;

import org.reflections.Reflections;

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.cfg.Packages;
import com.arvatosystems.t9t.init.InitContainers;

public final class Init {
    private Init() {
    }

    /** Initializes Jdp using defaults (including UI grid config init). */
    public static void initializeT9t() {
        initializeT9t(null, true);
    }

    /** Initializes Jdp using defaults (without grid config init for the UI). */
    public static void initializeT9tNoUi() {
        initializeT9t(null, false);
    }

    /** Initializes Jdp with the possibility to alter the automatically assigned classes before initialization. */
    public static void initializeT9t(final Consumer<Reflections[]> callback, final boolean initUi) {
        Jdp.reset();
        // Jdp.excludePackagePrefix("java.");
        Jdp.includePackagePrefix("de.jpaw.");
        Jdp.includePackagePrefix("com.arvatosystems.");
        Packages.walkExtraPackages((prefix, packageName) -> {
            if (packageName != null && !packageName.startsWith("com.arvatosystems.") && !packageName.startsWith("de.jpaw.")) {
                Jdp.includePackagePrefix(packageName + ".");
            }
        });

        final Reflections[] scannedPackages = initUi ? InitContainers.initializeT9tForUi() : InitContainers.initializeT9tForClients();
        Jdp.scanClasses(scannedPackages);
        if (callback != null) {
            callback.accept(scannedPackages);
        }
        Jdp.runStartups(scannedPackages);
    }
}
