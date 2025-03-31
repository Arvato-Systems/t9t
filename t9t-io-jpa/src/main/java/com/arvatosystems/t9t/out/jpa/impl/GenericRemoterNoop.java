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
package com.arvatosystems.t9t.out.jpa.impl;

import com.arvatosystems.t9t.out.services.IGenericRemoter;

import de.jpaw.bonaparte.core.BonaPortable;

public class GenericRemoterNoop implements IGenericRemoter {
    private int counter = 0;

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void send(BonaPortable data) {
        ++counter;
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public String getImplementation() {
        return "noop";
    }
}
