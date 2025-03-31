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
package com.arvatosystems.t9t.cfg.be;

import java.util.concurrent.atomic.AtomicBoolean;

public final class StatusProvider {
    private static final AtomicBoolean SHUTDOWN_IN_PROGRESS = new AtomicBoolean(false);

    private StatusProvider() { }

    public static void setShutdownInProgress() {
        SHUTDOWN_IN_PROGRESS.set(true);
    }

    public static boolean isShutdownInProgress() {
        return SHUTDOWN_IN_PROGRESS.get();
    }
}
