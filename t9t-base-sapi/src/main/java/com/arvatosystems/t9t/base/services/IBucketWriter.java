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
package com.arvatosystems.t9t.base.services;

import java.io.Closeable;
import java.util.Map;

import com.arvatosystems.t9t.base.event.BucketWriteKey;

/** A queue to write buckets. */
public interface IBucketWriter extends Closeable {
    /** Opens the output, issued as first call in regular lifecycle. */
    void open();

    /** Write to a set of buckets. */
    void writeToBuckets(Map<BucketWriteKey, Integer> cmds);

    /** Flushes unwritten buffers immediately - perform to sync.
     * Only required if the writer buffers data, otherwise a no-op. */
    default void flush() {}

    /** Shuts down the writer - last call in regular lifecycle. */
    @Override
    void close();
}
