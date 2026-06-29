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
package com.arvatosystems.t9t.base;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record IdAndName(@Nonnull String id, @Nullable String name) {
    /** Returns an IdAndName parsed from a string of the form "id:name". If no colon is present, name is null. */
    public static IdAndName of(@Nullable String pair) {
        if (pair == null) {
            return null;
        }
        final int colonPos = pair.indexOf(':');
        if (colonPos >= 0) {
            return new IdAndName(pair.substring(0, colonPos), pair.substring(colonPos + 1));
        } else {
            return new IdAndName(pair, null);
        }
    }
}
