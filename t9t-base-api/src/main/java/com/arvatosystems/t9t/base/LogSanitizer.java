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

public final class LogSanitizer {

    private LogSanitizer() {
        // empty, private to avoid instantiation
    }

    /**
     * Returns a sanitized representation of the passed object immediately.
     * Performs length checks as well as character checks.
     *
     * @param o
     * @return
     */
    public static String sanitize(final Object o) {
        if (o == null) {
            return "(null)";
        }
        final String s1 = o.toString();
        final String s2 = s1.length() > 36 ? s1.substring(0, 36) : s1;
        // fast check to see if we need sanitizing
        if (isClean(s2)) {
            return s2;
        }
        final StringBuilder sb = new StringBuilder(s2.length());
        for (int i = 0; i < s2.length(); ++i) {
            final char c = s2.charAt(i);
            if (c < ' ' || c > 0x7e) {
                sb.append(c);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    /** Checks if the string needs sanitizing at all, or is good to print as is (safes copying). */
    public static boolean isClean(final String s) {
        for (int i = 0; i < s.length(); ++i) {
            final char c = s.charAt(i);
            if (c < ' ' || c > 0x7e) {
                return false;
            }
        }
        return true;
    }

    private record TempWrapperForLog(Object o) {
        @Override
        public String toString() {
            return sanitize(o);
        }
    }

    /**
     * Returns a wrapper object which will provide a sanitized representation of the passed object once the toString() method is called.
     *
     * @param o
     * @return
     */
    public static Object sanitizeLazy(final Object o) {
        return new TempWrapperForLog(o);
    }
}
