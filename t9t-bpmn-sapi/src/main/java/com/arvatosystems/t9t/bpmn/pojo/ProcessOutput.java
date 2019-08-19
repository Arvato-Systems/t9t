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
package com.arvatosystems.t9t.bpmn.pojo;

import java.util.Collections;
import java.util.Map;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Represents the output of executing a particular BPMN process.
 */
public final class ProcessOutput {

    private Map<String, BonaPortable> output;

    /**
     * Creates a new ProcessOutput instance.
     *
     * @param output
     * @throws IllegalArgumentException
     *             if output is null
     */
    public ProcessOutput(final Map<String, BonaPortable> output) {
        if (output == null) {
            throw new IllegalArgumentException("Null is not allowed for constructing a ProcessOutput");
        }

        this.output = output;
    }

    /**
     * Gets the value for a given key from this process output
     *
     * @param outputKey
     *            the key for which the value is requested
     * @return the stored value for the given key. It may be null if the key is not present or if null is stored as a value for that key
     */
    public Object get(final String outputKey) {
        return output.get(outputKey);
    }

    /**
     * Get an unmodifiable copy of the internal map from this process output. Note, that only the key/value pairs are unmodifiable, the stored key- and value-objects can still be
     * changed.
     *
     * @return an unmodifiable map with all key/value pairs
     */
    public Map<String, BonaPortable> getAll() {
        return Collections.unmodifiableMap(output);
    }
}
