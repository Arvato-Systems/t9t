/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class RandomNumberGenerators {
    private RandomNumberGenerators() {
    }

    private static final long RANDOM_UUID_MSBS = UUID.randomUUID().getMostSignificantBits();

    /**
     * Provides a secure (cryptographic) random UUID.
     * This implementation is awfully slow (480 ns single threaded, up to 22000 ns in multithreaded corner cases),
     * but should be used for security reasons whenever dealing with access control or passwords.
     * It currently just wraps the Java standard method <code>UUID.randomUUID()</code>.
     */
    public static UUID randomSecureUUID() {
        return UUID.randomUUID();
    }

    /**
     * Provides a random UUID which can be used for test data or logging, or assigning unique IDs to requests.
     * This implementation is much faster than its secure counterpart (8 ns single threaded or multithreaded).
     * The most significant bits will be always the lifetime of one process.
     */
    public static UUID randomFastUUID() {
        return new UUID(RANDOM_UUID_MSBS, ThreadLocalRandom.current().nextLong());
    }

    /**
     * Provides a UUID which only consists of a portion (half of the bits are set to 0).
     * This serves as a signature of a server process.
     */
    public static UUID getInstanceSignatue() {
        return new UUID(RANDOM_UUID_MSBS, 0L);
    }

    /**
     * Provides a random long value.
     * This implementation uses <code>ThreadLocalRandom</code> which is much faster (takes 4 ns per invocation) than
     * the old class <code>Random<code> which is heavily synchronized (and took 20 ns per invocation).
     */
    public static long randomFastLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    /**
     * Provides a random int value.
     */
    public static int randomFastInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    /**
     * Provides a random int value for a given upper bound.
     */
    public static int randomFastInt(int max) {
        return ThreadLocalRandom.current().nextInt(max);
    }
}
