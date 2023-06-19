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
package com.arvatosystems.t9t.base.be.stubs;

import java.util.concurrent.atomic.AtomicLong;

import com.arvatosystems.t9t.base.services.ISingleRefGenerator;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

@Singleton
@Named("noop")
public class NopSingleRefGenerator implements ISingleRefGenerator {
    protected final AtomicLong counter = new AtomicLong();

    @Override
    public long getNextSequence(@Nonnull final String selectStatement) {
        return counter.incrementAndGet();
    }
}