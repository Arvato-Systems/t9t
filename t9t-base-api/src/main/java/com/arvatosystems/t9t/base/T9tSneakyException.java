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
package com.arvatosystems.t9t.base;

/**
 * Workaround to throw unchecked exceptions and avoid eclipse' dead code issue.
 */
public final class T9tSneakyException {

    /**
     * Private, do not in
     */
    private T9tSneakyException() {
        // empty to avoid instantiation
    }

    /**
     * Throw exception and return a value that will never be returned. Just to avoid dead code warnings.
     *
     * @param exc the {@link Throwable} to throw sneakily.
     * @return always null, or not? ;-)
     */
    public static RuntimeException throwIt(final Throwable exc) {
        T9tSneakyException.<RuntimeException>throwUnchecked(exc);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(final Throwable exc) throws T {
        throw (T) exc;
    }

}
