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

/** A very simple integer used as a reference, when the overhead of an AtomicInteger is not desired. */
public class MutableLong {
    private long value;

    public MutableLong(final long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(final long value) {
        this.value = value;
    }

    /** Adds another values and returns the sum. */
    public long add(final long b) {
        value += b;
        return value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MutableLong ml) {
            return value == ml.value;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
