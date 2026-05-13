/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.jwt;

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

public abstract class AbstractJWTPool implements IJWT {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJWTPool.class);
    protected final ThreadLocal<IJWT> pool = new ThreadLocal<>();

    // for injections where you are sure you have per thread scope as well
    protected IJWT get() {
        final IJWT jwt = pool.get();
        if (jwt != null) {
            return jwt;
        }
        LOGGER.info("Creating a new JWT for thread {}", Thread.currentThread().getName());
        final IJWT newJwt = createJwt();
        pool.set(newJwt);
        return newJwt;
    }

    @Override
    @Nonnull
    public JwtInfo decode(@Nonnull final String token) {
        return get().decode(token);
    }

    protected abstract IJWT createJwt();
}
