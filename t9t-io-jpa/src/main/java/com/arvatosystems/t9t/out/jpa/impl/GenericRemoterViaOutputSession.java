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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.services.IGenericRemoter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import jakarta.annotation.Nonnull;

public class GenericRemoterViaOutputSession implements IGenericRemoter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericRemoterViaOutputSession.class);
    private final Provider<IOutputSession> outputSessionProvider = Jdp.getProvider(IOutputSession.class);
    private final IOutputSession outputSession = outputSessionProvider.get();
    private int counter = 0;

    public GenericRemoterViaOutputSession(@Nonnull final OutputSessionParameters parameter) {
        outputSession.open(parameter);
    }

    @Override
    public void close() {
        try {
            outputSession.close();
        } catch (final Exception e) {
            LOGGER.error("Error closing output session", e);
            throw new T9tException(T9tIOException.IO_EXCEPTION, "Error closing output session" + e.getMessage());
        }
    }

    @Override
    public void send(final BonaPortable data) {
        ++counter;
        outputSession.store(data);
    }

    @Override
    public int getCounter() {
        return counter;
    }

    @Override
    public String getImplementation() {
        return "os";
    }
}
